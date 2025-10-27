package com.rehabai.file_service.service;

import com.rehabai.file_service.events.FileUploadedEvent;
import com.rehabai.file_service.model.IngestionFile;
import com.rehabai.file_service.repository.IngestionFileRepository;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class StorageService {

    private final S3Client s3;
    private final IngestionFileRepository repo;
    private final RabbitTemplate rabbit;
    private final DirectExchange exchange;
    private final String bucket;
    private final String routingKeyUploaded;

    public StorageService(S3Client s3,
                          IngestionFileRepository repo,
                          RabbitTemplate rabbit,
                          DirectExchange exchange,
                          @Value("${s3.bucket}") String bucket,
                          @Value("${amqp.routingKeyUploaded:file.uploaded}") String routingKeyUploaded) {
        this.s3 = s3;
        this.repo = repo;
        this.rabbit = rabbit;
        this.exchange = exchange;
        this.bucket = bucket;
        this.routingKeyUploaded = routingKeyUploaded;
    }

    public IngestionFile upload(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String key = "uploads/" + UUID.randomUUID() + (original != null ? ("_" + original) : "");

        // upload to S3
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();
        s3.putObject(put, RequestBody.fromBytes(file.getBytes()));

        // persist metadata
        IngestionFile ent = new IngestionFile();
        ent.setOriginalName(original);
        ent.setS3Path(key);
        ent.setSizeBytes(file.getSize());
        ent.setHashSha256(sha256(file.getBytes()));
        IngestionFile saved = repo.save(ent);

        // publish event
        FileUploadedEvent evt = new FileUploadedEvent(saved.getId(), bucket, key, original, file.getSize(), saved.getHashSha256());
        rabbit.convertAndSend(exchange.getName(), routingKeyUploaded, evt);

        return saved;
    }

    private String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

