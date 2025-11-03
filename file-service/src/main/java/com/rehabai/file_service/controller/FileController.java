package com.rehabai.file_service.controller;

import com.rehabai.file_service.security.SecurityHelper;
import com.rehabai.file_service.model.AnonymizationLog;
import com.rehabai.file_service.model.FileStatus;
import com.rehabai.file_service.model.IngestionFile;
import com.rehabai.file_service.service.AnonymizationLogService;
import com.rehabai.file_service.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;
    private final AnonymizationLogService anonymizationLogService;
    private final SecurityHelper securityHelper;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<IngestionFile> upload(@RequestPart("file") MultipartFile file,
                                                @RequestParam("userId") UUID userId) throws IOException {
        securityHelper.requireClinician();
        IngestionFile saved = storageService.upload(file, userId);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/pseudonymize")
    public ResponseEntity<IngestionFile> pseudonymize(@PathVariable UUID id) {
        securityHelper.requireClinician();
        IngestionFile updated = storageService.pseudonymize(id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/anonymization-logs")
    public ResponseEntity<List<AnonymizationLog>> listAnonymizationLogs(@PathVariable UUID id) {
        securityHelper.requireClinician();
        return ResponseEntity.ok(anonymizationLogService.listByFile(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngestionFile> get(@PathVariable UUID id) {
        IngestionFile f = storageService.get(id);
        securityHelper.validateResourceAccess(f.getUserId());
        return ResponseEntity.ok(f);
    }

    @GetMapping
    public ResponseEntity<List<IngestionFile>> list(@RequestParam(required = false) UUID userId,
                                                    @RequestParam(required = false) FileStatus status) {
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        boolean isStaff = securityHelper.hasAnyRole("ADMIN", "CLINICIAN");

        if (!isStaff) {
            if (userId != null && !userId.equals(authenticatedUserId)) {
                throw new IllegalArgumentException("Access denied: You can only list your own files");
            }
            userId = authenticatedUserId;
        } else {
            // CLINICIAN/ADMIN must specify userId
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(storageService.list(userId, status));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) throws IOException {
        IngestionFile f = storageService.get(id);
        securityHelper.validateResourceAccess(f.getUserId());

        byte[] data = storageService.download(id);
        String filename = f.getOriginalName() != null ? f.getOriginalName() : "file";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        IngestionFile f = storageService.get(id);
        securityHelper.validateResourceAccess(f.getUserId());
        storageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
