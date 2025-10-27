package com.rehabai.prescription_service.consumer;

import com.rehabai.prescription_service.events.FileUploadedEvent;
import com.rehabai.prescription_service.service.PipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Component
public class FileUploadedListener {

    private static final Logger log = LoggerFactory.getLogger(FileUploadedListener.class);

    private final S3Client s3Client;
    private final PipelineService pipelineService;

    public FileUploadedListener(S3Client s3Client, PipelineService pipelineService) {
        this.s3Client = s3Client;
        this.pipelineService = pipelineService;
    }

    @RabbitListener(queues = "${amqp.prescriptionQueue:prescription.file.uploaded}")
    public void handle(FileUploadedEvent event) {
        log.info("[Prescription] Received file.uploaded: id={}, bucket={}, key={}", event.id(), event.bucket(), event.s3Path());
        try {
            ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(event.bucket())
                    .key(event.s3Path())
                    .build());
            byte[] content = bytes.asByteArray();
            String contentType = bytes.response() != null ? bytes.response().contentType() : null;
            log.info("Downloaded file bytes: {} bytes (contentType={}). Starting pipeline...", content.length, contentType);
            pipelineService.processFile(event.id(), content, event.originalName(), contentType);
            log.info("[Prescription] Pipeline completed for file {}", event.id());
        } catch (Exception e) {
            log.error("[Prescription] Error processing file {}: {}", event.id(), e.getMessage(), e);
            // TODO: handle error status and publish failure event if needed
        }
    }
}
