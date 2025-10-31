package com.rehabai.file_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(@Value("${AWS_ACCESS_KEY_ID}") String accessKey,
                             @Value("${AWS_SECRET_ACCESS_KEY}") String secretKey,
                             @Value("${AWS_REGION:us-east-1}") String region,
                             @Value("${S3_ENDPOINT:}") String endpoint) {
        var creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(creds);
        if (endpoint != null && !endpoint.isBlank()) {
            builder = builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }
}
