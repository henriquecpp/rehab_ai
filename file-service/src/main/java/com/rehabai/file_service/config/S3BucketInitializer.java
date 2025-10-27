package com.rehabai.file_service.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Configuration
public class S3BucketInitializer {

    private static final Logger log = LoggerFactory.getLogger(S3BucketInitializer.class);

    private final S3Client s3;
    private final String bucket;

    public S3BucketInitializer(S3Client s3, @Value("${s3.bucket}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @PostConstruct
    public void ensureBucket() {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("S3 bucket '{}' exists", bucket);
        } catch (Exception ex) {
            log.info("S3 bucket '{}' not found. Creating...", bucket);
            try {
                s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("S3 bucket '{}' created", bucket);
            } catch (Exception ce) {
                log.warn("Failed to create S3 bucket '{}': {}", bucket, ce.getMessage());
            }
        }
    }
}

