package com.rehabai.patient_service.controller;

import com.rehabai.patient_service.dto.PatientDtos;
import com.rehabai.patient_service.service.PatientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/patients/{userId}/profile")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientProfileService service;

    @GetMapping
    public ResponseEntity<PatientDtos.ProfileResponse> get(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.get(userId));
    }

    @PutMapping
    public ResponseEntity<PatientDtos.ProfileResponse> upsert(@PathVariable UUID userId,
                                                              @Valid @RequestBody PatientDtos.ProfileRequest req) {
        return ResponseEntity.ok(service.upsert(userId, req));
    }
}
