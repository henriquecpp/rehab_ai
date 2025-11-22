package com.rehabai.auth_service.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(this::clientHttpRequestFactory)
                .additionalInterceptors(headerPropagation(), loggingInterceptor())
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return factory;
    }

    private ClientHttpRequestInterceptor headerPropagation() {
        return (request, body, execution) -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest current = attrs.getRequest();
                copyHeader(current, request, "X-User-Id");
                copyHeader(current, request, "X-User-Roles");
                copyHeader(current, request, "X-User-Email");
                copyHeader(current, request, "Authorization");
            }
            return execution.execute(request, body);
        };
    }

    private static void copyHeader(HttpServletRequest source, org.springframework.http.HttpRequest target, String name) {
        String value = source.getHeader(name);
        if (value != null && !value.isBlank()) {
            target.getHeaders().add(name, value);
        }
    }

    private ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            long start = System.currentTimeMillis();
            try {
                var response = execution.execute(request, body);
                long duration = System.currentTimeMillis() - start;
                if (duration > 1000) {
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
