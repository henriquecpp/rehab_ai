package com.rehabai.file_service.controller;

import com.rehabai.file_service.model.IngestionFile;
import com.rehabai.file_service.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {

    private final StorageService storageService;

    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<IngestionFile> upload(@RequestPart("file") MultipartFile file) throws IOException {
        IngestionFile saved = storageService.upload(file);
        return ResponseEntity.ok(saved);
    }
}

