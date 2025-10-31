package com.rehabai.user_service.controller;

import com.rehabai.user_service.dto.ConsentDtos;
import com.rehabai.user_service.dto.UserDtos;
import com.rehabai.user_service.service.ConsentService;
import com.rehabai.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
public class ConsentController {

    private final ConsentService service;
    private final UserService userService;

    public ConsentController(ConsentService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    // Endpoints baseados em userId explícito
    @PostMapping("/users/{userId}/consents")
    public ResponseEntity<ConsentDtos.Response> create(@PathVariable UUID userId,
                                                       @Valid @RequestBody ConsentDtos.CreateRequest req) {
        ConsentDtos.Response created = service.create(userId, req);
        return ResponseEntity.created(URI.create("/users/" + userId + "/consents/" + created.id())).body(created);
    }

    @PostMapping("/users/{userId}/consents/revoke")
    public ResponseEntity<ConsentDtos.Response> revoke(@PathVariable UUID userId,
                                                       @Valid @RequestBody ConsentDtos.RevokeRequest req) {
        ConsentDtos.Response revoked = service.revoke(userId, req);
        return ResponseEntity.ok(revoked);
    }

    @GetMapping("/users/{userId}/consents")
    public ResponseEntity<List<ConsentDtos.Response>> list(@PathVariable UUID userId,
                                                           @RequestParam(required = false) String type) {
        return ResponseEntity.ok(service.listByUser(userId, type));
    }

    @GetMapping("/users/{userId}/consents/latest")
    public ResponseEntity<ConsentDtos.Response> latest(@PathVariable UUID userId,
                                                       @RequestParam String type) {
        return ResponseEntity.ok(service.latestByType(userId, type));
    }

    // Endpoints para o usuário autenticado (derivados do JWT)
    @PostMapping("/users/me/consents")
    public ResponseEntity<ConsentDtos.Response> createForMe(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody ConsentDtos.CreateRequest req) {
        UUID userId = resolveUserId(jwt);
        ConsentDtos.Response created = service.create(userId, req);
        return ResponseEntity.created(URI.create("/users/me/consents/" + created.id())).body(created);
    }

    @PostMapping("/users/me/consents/revoke")
    public ResponseEntity<ConsentDtos.Response> revokeForMe(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody ConsentDtos.RevokeRequest req) {
        UUID userId = resolveUserId(jwt);
        return ResponseEntity.ok(service.revoke(userId, req));
    }

    @GetMapping("/users/me/consents")
    public ResponseEntity<List<ConsentDtos.Response>> listForMe(@AuthenticationPrincipal Jwt jwt,
                                                                @RequestParam(required = false) String type) {
        UUID userId = resolveUserId(jwt);
        return ResponseEntity.ok(service.listByUser(userId, type));
    }

    private UUID resolveUserId(Jwt jwt) {
        Object uid = jwt.getClaim("user_id");
        if (uid instanceof String s) {
            try { return UUID.fromString(s); } catch (IllegalArgumentException ignored) {}
        }
        // Fallback: usar subject (email) e buscar usuário
        String email = jwt.getSubject();
        UserDtos.Response u = userService.getByEmail(email);
        return u.id();
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
