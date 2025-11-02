package com.rehabai.auth_service.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(this::clientHttpRequestFactory)
                .additionalInterceptors(loggingInterceptor())
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);     // 5s to establish connection
        factory.setReadTimeout(10000);       // 10s to read response
        return factory;
    }

    private ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            long start = System.currentTimeMillis();
            try {
                var response = execution.execute(request, body);
                long duration = System.currentTimeMillis() - start;
                if (duration > 1000) {
                    // Log slow calls
                    org.slf4j.LoggerFactory.getLogger(RestClientConfig.class)
                        .warn("Slow HTTP call: {} {} took {}ms",
                              request.getMethod(), request.getURI(), duration);
                }
                return response;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - start;
                org.slf4j.LoggerFactory.getLogger(RestClientConfig.class)
                    .error("HTTP call failed: {} {} after {}ms - {}",
                           request.getMethod(), request.getURI(), duration, e.getMessage());
                throw e;
            }
        };
    }
}

