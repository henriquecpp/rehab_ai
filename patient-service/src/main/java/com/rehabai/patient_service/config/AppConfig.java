package com.rehabai.patient_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);
        f.setReadTimeout(5000);
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
        rt.getInterceptors().add(headerPropagation);
        return rt;
    }

    private static void copyHeader(HttpServletRequest source, org.springframework.http.HttpRequest target, String name) {
        String value = source.getHeader(name);
        if (value != null && !value.isBlank()) {
            target.getHeaders().add(name, value);
        }
    }
}
