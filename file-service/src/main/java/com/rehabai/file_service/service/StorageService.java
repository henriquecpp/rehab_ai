package com.rehabai.file_service.service;

import com.rehabai.file_service.events.FileUploadedEvent;
import com.rehabai.file_service.model.IngestionFile;
import com.rehabai.file_service.model.FileStatus;
import com.rehabai.file_service.repository.IngestionFileRepository;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private final S3Client s3;
    private final IngestionFileRepository repo;
    private final RabbitTemplate rabbit;
    private final DirectExchange exchange;
    private final String bucket;
    private final String routingKeyUploaded;
    private final AnonymizationLogService anonymizationLogService;

    public StorageService(S3Client s3,
                          IngestionFileRepository repo,
                          RabbitTemplate rabbit,
                          DirectExchange exchange,
                          @Value("${s3.bucket}") String bucket,
                          @Value("${amqp.routingKeyUploaded:file.uploaded}") String routingKeyUploaded,
                          AnonymizationLogService anonymizationLogService) {
        this.s3 = s3;
        this.repo = repo;
        this.rabbit = rabbit;
        this.exchange = exchange;
        this.bucket = bucket;
        this.routingKeyUploaded = routingKeyUploaded;
        this.anonymizationLogService = anonymizationLogService;
    }

    public IngestionFile upload(MultipartFile file, UUID userId) throws IOException {
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
        ent.setUserId(userId);
        ent.setOriginalName(original);
        ent.setS3Path(key);
        ent.setStatus(FileStatus.UPLOADED);
        ent.setSizeBytes(file.getSize());
        ent.setHashSha256(sha256(file.getBytes()));
        IngestionFile saved = repo.save(ent);

        // publish event
        FileUploadedEvent evt = new FileUploadedEvent(saved.getId(), bucket, key, original, file.getSize(), saved.getHashSha256());
        rabbit.convertAndSend(exchange.getName(), routingKeyUploaded, evt);

        return saved;
    }

    public IngestionFile pseudonymize(UUID fileId) {
        IngestionFile file = repo.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("file_not_found"));
        try {
            String srcKey = file.getS3Path();
            String anonymizedKey = "anonymized/" + file.getId() + "/" + (file.getOriginalName() != null ? file.getOriginalName() : "file");

            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                    .copySource(bucket + "/" + srcKey)
                    .destinationBucket(bucket)
                    .destinationKey(anonymizedKey)
                    .build();
            s3.copyObject(copyReq);

            // update file metadata
            String oldPath = file.getS3Path();
            file.setS3Path(anonymizedKey);
            String oldName = file.getOriginalName();
            if (oldName != null) {
                // mask original name
                file.setOriginalName("[REDACTED]");
                anonymizationLogService.add(fileId, "mask_original_name", "original_name");
            }
            anonymizationLogService.add(fileId, "s3_path_relocation", "s3_path: " + oldPath + " -> " + anonymizedKey);
            file.setStatus(FileStatus.ANONYMIZED);

            return repo.save(file);
        } catch (S3Exception e) {
            file.setStatus(FileStatus.ERROR);
            repo.save(file);
            anonymizationLogService.add(fileId, "pseudonymize_error", e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage());
            throw new IllegalStateException("pseudonymize_failed", e);
        }
    }

    // New helper methods
    public IngestionFile get(UUID id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("file_not_found"));
    }

    public List<IngestionFile> list(UUID userId, FileStatus status) {
        if (status == null) {
            return repo.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return repo.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
    }

    public byte[] download(UUID id) throws IOException {
        IngestionFile file = get(id);
        GetObjectRequest req = GetObjectRequest.builder().bucket(bucket).key(file.getS3Path()).build();
        try (var is = s3.getObject(req)) {
            return is.readAllBytes();
        }
    }

    public void delete(UUID id) {
        IngestionFile file = get(id);
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(file.getS3Path()).build());
        } catch (S3Exception e) {
            // ignore if not found in S3, proceed to delete metadata
        }
        repo.deleteById(id);
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
