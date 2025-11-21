package com.rehabai.plan_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public RestTemplate restTemplate(
            @Value("${patient.service.timeout-ms:3000}") int patientTimeout,
            @Value("${patient.service.retry.max-attempts:3}") int patientMaxAttempts,
            @Value("${patient.service.retry.backoff-ms:300}") int patientBackoff,
            @Value("${user.service.timeout-ms:3000}") int userTimeout,
            @Value("${user.service.retry.max-attempts:3}") int userMaxAttempts,
            @Value("${user.service.retry.backoff-ms:300}") int userBackoff
    ) {
        int connectTimeout = Math.max(patientTimeout, userTimeout);
        int readTimeout = connectTimeout;

        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(connectTimeout);
        f.setReadTimeout(readTimeout);
        RestTemplate rt = new RestTemplate(f);

        ClientHttpRequestInterceptor headerPropagation = (request, body, execution) -> {
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

        ClientHttpRequestInterceptor retryInterceptor = (request, body, execution) -> {
            boolean isGet = "GET".equalsIgnoreCase(request.getMethod().name());
            int maxAttempts = isGet ? (request.getURI().toString().contains("/patients/") ? patientMaxAttempts : userMaxAttempts) : 1;
            int backoff = request.getURI().toString().contains("/patients/") ? patientBackoff : userBackoff;
            int attempt = 0;
            while (true) {
                try {
                    return execution.execute(request, body);
                } catch (Exception ex) {
                    attempt++;
                    if (attempt >= maxAttempts) {
                        throw ex;
                    }
                    try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        };

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(rt.getInterceptors());
        interceptors.add(headerPropagation);
        interceptors.add(retryInterceptor);
        rt.setInterceptors(interceptors);
        return rt;
    }

    private static void copyHeader(HttpServletRequest source, org.springframework.http.HttpRequest target, String name) {
        String value = source.getHeader(name);
        if (value != null && !value.isBlank()) {
            target.getHeaders().add(name, value);
        }
    }
}
