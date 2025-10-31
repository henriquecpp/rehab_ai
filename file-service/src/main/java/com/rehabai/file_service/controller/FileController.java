package com.rehabai.file_service.controller;

import com.rehabai.file_service.model.AnonymizationLog;
import com.rehabai.file_service.model.FileStatus;
import com.rehabai.file_service.model.IngestionFile;
import com.rehabai.file_service.service.AnonymizationLogService;
import com.rehabai.file_service.service.StorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private final StorageService storageService;
    private final AnonymizationLogService anonymizationLogService;

    public FileController(StorageService storageService, AnonymizationLogService anonymizationLogService) {
        this.storageService = storageService;
        this.anonymizationLogService = anonymizationLogService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<IngestionFile> upload(@RequestPart("file") MultipartFile file,
                                                @RequestParam("userId") UUID userId) throws IOException {
        IngestionFile saved = storageService.upload(file, userId);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    @PostMapping("/{id}/pseudonymize")
    public ResponseEntity<IngestionFile> pseudonymize(@PathVariable UUID id) {
        IngestionFile updated = storageService.pseudonymize(id);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    @GetMapping("/{id}/anonymization-logs")
    public ResponseEntity<List<AnonymizationLog>> listAnonymizationLogs(@PathVariable UUID id) {
        return ResponseEntity.ok(anonymizationLogService.listByFile(id));
    }

    // New endpoints
    @GetMapping("/{id}")
    public ResponseEntity<IngestionFile> get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, Authentication auth) {
        IngestionFile f = storageService.get(id);
        if (!canAccess(jwt, auth, f.getUserId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(f);
    }

    @GetMapping
    public ResponseEntity<List<IngestionFile>> list(@RequestParam(required = false) UUID userId,
                                                    @RequestParam(required = false) FileStatus status,
                                                    @AuthenticationPrincipal Jwt jwt,
                                                    Authentication auth) {
        UUID requester = resolveUserId(jwt);
        boolean isStaff = hasRole(auth, "ROLE_ADMIN") || hasRole(auth, "ROLE_CLINICIAN");
        if (!isStaff) {
            if (userId != null && !userId.equals(requester)) {
                return ResponseEntity.status(403).build();
            }
            userId = requester;
        } else {
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(storageService.list(userId, status));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id,
                                           @AuthenticationPrincipal Jwt jwt,
                                           Authentication auth) throws IOException {
        IngestionFile f = storageService.get(id);
        if (!canAccess(jwt, auth, f.getUserId())) {
            return ResponseEntity.status(403).build();
        }
        byte[] data = storageService.download(id);
        String filename = f.getOriginalName() != null ? f.getOriginalName() : "file";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal Jwt jwt,
                                       Authentication auth) {
        IngestionFile f = storageService.get(id);
        if (!canAccess(jwt, auth, f.getUserId())) {
            return ResponseEntity.status(403).build();
        }
        storageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean canAccess(Jwt jwt, Authentication auth, UUID ownerId) {
        if (hasRole(auth, "ROLE_ADMIN") || hasRole(auth, "ROLE_CLINICIAN")) return true;
        UUID requester = resolveUserId(jwt);
        return requester != null && requester.equals(ownerId);
    }

    private boolean hasRole(Authentication auth, String role) {
        if (auth == null) return false;
        Collection<? extends GrantedAuthority> auths = auth.getAuthorities();
        if (auths == null) return false;
        return auths.stream().anyMatch(a -> role.equals(a.getAuthority()));
    }

    private UUID resolveUserId(Jwt jwt) {
        if (jwt == null) return null;
        Object uid = jwt.getClaim("user_id");
        if (uid instanceof String s) {
            try { return UUID.fromString(s); } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }
}
