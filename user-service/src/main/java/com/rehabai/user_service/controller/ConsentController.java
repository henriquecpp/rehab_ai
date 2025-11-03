package com.rehabai.user_service.controller;

import com.rehabai.user_service.security.SecurityHelper;
import com.rehabai.user_service.dto.ConsentDtos;
import com.rehabai.user_service.service.ConsentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for user consent management (LGPD/GDPR compliance).
 * Security handled by API Gateway (validates JWT and injects headers).
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ConsentController {

    private final ConsentService service;
    private final SecurityHelper securityHelper;

    // Endpoints baseados em userId explícito
    @PostMapping("/users/{userId}/consents")
    public ResponseEntity<ConsentDtos.Response> create(@PathVariable UUID userId,
                                                       @Valid @RequestBody ConsentDtos.CreateRequest req) {
        // Validate access: user can only manage their own consents
        securityHelper.validateResourceAccess(userId);
        ConsentDtos.Response created = service.create(userId, req);
        return ResponseEntity.created(URI.create("/users/" + userId + "/consents/" + created.id())).body(created);
    }

    @PostMapping("/users/{userId}/consents/revoke")
    public ResponseEntity<ConsentDtos.Response> revoke(@PathVariable UUID userId,
                                                       @Valid @RequestBody ConsentDtos.RevokeRequest req) {
        // Validate access: user can only revoke their own consents
        securityHelper.validateResourceAccess(userId);
        ConsentDtos.Response revoked = service.revoke(userId, req);
        return ResponseEntity.ok(revoked);
    }

    @GetMapping("/users/{userId}/consents")
    public ResponseEntity<List<ConsentDtos.Response>> list(@PathVariable UUID userId,
                                                           @RequestParam(required = false) String type) {
        // Validate access: user can only see their own consents
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.listByUser(userId, type));
    }

    @GetMapping("/users/{userId}/consents/latest")
    public ResponseEntity<ConsentDtos.Response> latest(@PathVariable UUID userId,
                                                       @RequestParam String type) {
        // Validate access: user can only see their own consents
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.latestByType(userId, type));
    }

    // Endpoints para o usuário autenticado (usando X-User-Id header)
    @PostMapping("/users/me/consents")
    public ResponseEntity<ConsentDtos.Response> createForMe(@Valid @RequestBody ConsentDtos.CreateRequest req) {
        UUID userId = securityHelper.getAuthenticatedUserId();
        ConsentDtos.Response created = service.create(userId, req);
        return ResponseEntity.created(URI.create("/users/me/consents/" + created.id())).body(created);
    }

    @PostMapping("/users/me/consents/revoke")
    public ResponseEntity<ConsentDtos.Response> revokeForMe(@Valid @RequestBody ConsentDtos.RevokeRequest req) {
        UUID userId = securityHelper.getAuthenticatedUserId();
        return ResponseEntity.ok(service.revoke(userId, req));
    }

    @GetMapping("/users/me/consents")
    public ResponseEntity<List<ConsentDtos.Response>> listForMe(@RequestParam(required = false) String type) {
        UUID userId = securityHelper.getAuthenticatedUserId();
        return ResponseEntity.ok(service.listByUser(userId, type));
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "bad_request";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (msg.startsWith("user_not_found") || msg.equals("consent_not_found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (msg.equals("type_required")) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }
}
