package com.rehabai.plan_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Value("${logging.request-body.enabled:false}")
    private boolean requestBodyLoggingEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Só loga se estiver habilitado E (em dev OU explicitamente configurado)
        boolean shouldLog = requestBodyLoggingEnabled || "dev".equalsIgnoreCase(activeProfile);

        if (!shouldLog || !isLoggableRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            logRequest(wrappedRequest, wrappedResponse);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private boolean isLoggableRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        return ("POST".equalsIgnoreCase(method) ||
                "PUT".equalsIgnoreCase(method) ||
                "PATCH".equalsIgnoreCase(method)) &&
                !path.contains("/actuator");
    }

    private void logRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        byte[] content = request.getContentAsByteArray();
        String body = content.length > 0
            ? new String(content, StandardCharsets.UTF_8)
            : "<empty>";

        if (body.length() > 2048) {
            body = body.substring(0, 2048) + "... [truncated]";
        }

        log.info("REQUEST: {} {} | Status: {} | Body: {}", method, uri, status, body);

        if (status >= 400 && status < 500) {
            byte[] responseContent = response.getContentAsByteArray();
            if (responseContent.length > 0) {
                String responseBody = new String(responseContent, StandardCharsets.UTF_8);
                log.warn("RESPONSE ({}): {}", status, responseBody);
            }
        }
    }
}

