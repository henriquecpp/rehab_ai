package com.rehabai.patient_service.controller;

import com.rehabai.patient_service.dto.PatientDtos;
import com.rehabai.patient_service.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService service;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    @PostMapping
    public ResponseEntity<PatientDtos.Response> create(@Valid @RequestBody PatientDtos.CreateRequest req) {
        PatientDtos.Response created = service.create(req);
        return ResponseEntity.created(URI.create("/patients/" + created.id())).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    @GetMapping
    public ResponseEntity<List<PatientDtos.Response>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<PatientDtos.Response> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<PatientDtos.Response> update(@PathVariable UUID id, @Valid @RequestBody PatientDtos.UpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
