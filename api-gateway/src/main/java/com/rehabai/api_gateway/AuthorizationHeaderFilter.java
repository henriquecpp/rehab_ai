package com.rehabai.api_gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Simple servlet filter that ensures the Authorization header is present and can be logged or modified.
 * This filter does not remove or mutate the header; it just ensures it's available for downstream services.
 */
@Component
public class AuthorizationHeaderFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationHeaderFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && !auth.isEmpty()) {
            // Optionally, you could validate format or log
            log.debug("Authorization header present, length={}", auth.length());
        }
        filterChain.doFilter(request, response);
    }
}

