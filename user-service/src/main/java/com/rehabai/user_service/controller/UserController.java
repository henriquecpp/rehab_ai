package com.rehabai.user_service.controller;

import com.rehabai.user_service.security.SecurityHelper;
import com.rehabai.user_service.dto.UserDtos;
import com.rehabai.user_service.model.UserRole;
import com.rehabai.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final SecurityHelper securityHelper;

    @GetMapping("/users/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/users")
    public ResponseEntity<UserDtos.Response> create(@Valid @RequestBody UserDtos.CreateRequest req) {
        UserDtos.Response created = service.create(req);
        return ResponseEntity.created(URI.create("/users/" + created.id())).body(created);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDtos.Response>> list(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean activeOnly) {

        if (!securityHelper.hasAnyRole("CLINICIAN", "ADMIN")) {
            throw new IllegalArgumentException("Access denied: CLINICIAN or ADMIN role required");
        }

        if (role != null) {
            return ResponseEntity.ok(service.listByRole(role));
        }
        if (Boolean.TRUE.equals(activeOnly)) {
            return ResponseEntity.ok(service.listActive());
        }
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDtos.Response> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<UserDtos.Response> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.getByEmail(email));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDtos.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserDtos.UpdateRequest req) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        securityHelper.requireAdmin();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        service.delete(id, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/users/credentials")
    public ResponseEntity<UserDtos.CredentialsResponse> getCredentials(@RequestParam String email) {
        return ResponseEntity.ok(service.getCredentialsByEmail(email));
    }

    @GetMapping("/internal/users/count")
    public ResponseEntity<Long> countUsers() {
        return ResponseEntity.ok(service.count());
    }

    @GetMapping("/internal/users/any-admin")
    public ResponseEntity<Boolean> anyAdmin() {
        return ResponseEntity.ok(service.anyAdminExists());
    }

    @PostMapping("/users/{id}/activate")
    public ResponseEntity<UserDtos.Response> activate(@PathVariable UUID id) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(service.activate(id));
    }

    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<UserDtos.Response> deactivate(@PathVariable UUID id) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(service.deactivate(id));
    }

    @PostMapping("/users/{id}/role")
    public ResponseEntity<UserDtos.Response> changeRole(@PathVariable UUID id,
                                                        @Valid @RequestBody UserDtos.ChangeRoleRequest req) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(service.changeRole(id, req.role()));
    }

    @PostMapping("/users/{id}/password")
    public ResponseEntity<UserDtos.Response> changePassword(@PathVariable UUID id,
                                                            @Valid @RequestBody UserDtos.ChangePasswordRequest req) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(service.changePassword(id, req.passwordHash()));
    }
}
