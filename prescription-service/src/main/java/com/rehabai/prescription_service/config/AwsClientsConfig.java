package com.rehabai.prescription_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class AwsClientsConfig {

    @Bean
    public TextractClient textractClient(@Value("${AWS_REGION:us-east-1}") String region) {
        return TextractClient.builder().region(Region.of(region)).build();
    }

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient(@Value("${AWS_REGION:us-east-1}") String region) {
        return BedrockRuntimeClient.builder().region(Region.of(region)).build();
    }
}

