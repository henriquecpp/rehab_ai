package com.rehabai.file_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.utils.StringUtils;

import java.net.URI;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(
            @Value("${AWS_ACCESS_KEY_ID:dummy}") String accessKey,
            @Value("${AWS_SECRET_ACCESS_KEY:dummy}") String secretKey,
            @Value("${AWS_REGION:us-east-1}") String region,
            @Value("${S3_ENDPOINT:}") String endpoint
    ) {
        S3Configuration s3conf = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .serviceConfiguration(s3conf)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .overrideConfiguration(ClientOverrideConfiguration.builder().build());

        if (StringUtils.isNotBlank(endpoint)) {
            builder = builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}
