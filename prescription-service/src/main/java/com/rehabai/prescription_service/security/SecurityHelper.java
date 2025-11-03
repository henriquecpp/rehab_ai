package com.rehabai.prescription_service.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Security helper that reads user information from HTTP headers injected by API Gateway.
 *
 * IMPORTANT: This service DOES NOT validate JWT. It trusts headers from Gateway.
 *
 * Flow:
 * 1. Client → Gateway: Authorization: Bearer <jwt>
 * 2. Gateway validates JWT and extracts claims
 * 3. Gateway → prescription-service: X-User-Id, X-User-Roles (NO JWT!)
 * 4. prescription-service uses this helper to read headers
 */
@Component
public class SecurityHelper {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_EMAIL = "X-User-Email";

    public UUID getAuthenticatedUserId() {
        String userId = getHeader(HEADER_USER_ID);
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("X-User-Id header missing - request not authenticated");
        }
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid X-User-Id format: " + userId);
        }
    }

    public String getAuthenticatedUserEmail() {
        String email = getHeader(HEADER_USER_EMAIL);
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("X-User-Email header missing");
        }
        return email;
    }

    public boolean hasRole(String role) {
        List<String> roles = getRoles();
        String roleToCheck = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return roles.contains(roleToCheck);
    }

    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public void requireAdmin() {
        if (!hasRole("ADMIN")) {
            throw new IllegalArgumentException("Access denied: This operation requires ADMIN role");
        }
    }

    public void requireClinician() {
        if (!hasAnyRole("CLINICIAN", "ADMIN")) {
            throw new IllegalArgumentException("Access denied: This operation requires CLINICIAN or ADMIN role");
        }
    }

    public void validateResourceAccess(UUID targetUserId) {
        UUID authenticatedUserId = getAuthenticatedUserId();

        if (hasAnyRole("CLINICIAN", "ADMIN")) {
            return;
        }

        if (!authenticatedUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("Access denied: You can only access your own resources");
        }
    }

    private List<String> getRoles() {
        String rolesHeader = getHeader(HEADER_USER_ROLES);
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return List.of();
        }
        return Arrays.asList(rolesHeader.split(","));
    }

    private String getHeader(String headerName) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request.getHeader(headerName);
    }
}

