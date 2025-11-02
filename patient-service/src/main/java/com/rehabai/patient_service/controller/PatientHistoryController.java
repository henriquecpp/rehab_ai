package com.rehabai.patient_service.controller;

import com.rehabai.patient_service.dto.PatientDtos;
import com.rehabai.patient_service.security.SecurityHelper;
import com.rehabai.patient_service.service.PatientHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for patient history (notes, conditions, allergies, medications, vitals).
 * CRITICAL: All endpoints validate patient access to prevent data leaks.
 */
@RestController
@RequestMapping("/patients/{userId}")
@RequiredArgsConstructor
public class PatientHistoryController {

    private final PatientHistoryService service;
    private final SecurityHelper securityHelper;

    // Notes
    @PostMapping("/history/notes")
    public ResponseEntity<PatientDtos.NoteResponse> addNote(@PathVariable UUID userId,
                                                            @Valid @RequestBody PatientDtos.NoteCreateRequest req) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.addNote(userId, req));
    }

    @GetMapping("/history/notes")
    public ResponseEntity<List<PatientDtos.NoteResponse>> listNotes(@PathVariable UUID userId) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.listNotes(userId));
    }

    // Conditions
    @PostMapping("/conditions")
    public ResponseEntity<PatientDtos.ConditionResponse> addCondition(@PathVariable UUID userId,
                                                                      @Valid @RequestBody PatientDtos.ConditionCreateRequest req) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.addCondition(userId, req));
    }

    @GetMapping("/conditions")
    public ResponseEntity<List<PatientDtos.ConditionResponse>> listConditions(@PathVariable UUID userId) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.listConditions(userId));
    }

    @GetMapping("/conditions/{conditionId}")
    public ResponseEntity<PatientDtos.ConditionResponse> getCondition(@PathVariable UUID userId,
                                                                      @PathVariable UUID conditionId) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.getCondition(userId, conditionId));
    }

    @DeleteMapping("/conditions/{conditionId}")
    public ResponseEntity<Void> deleteCondition(@PathVariable UUID userId,
                                                @PathVariable UUID conditionId) {
        securityHelper.validatePatientAccess(userId);
        service.deleteCondition(userId, conditionId);
        return ResponseEntity.noContent().build();
    }

    // Allergies
    @PostMapping("/allergies")
    public ResponseEntity<PatientDtos.AllergyResponse> addAllergy(@PathVariable UUID userId,
                                                                  @Valid @RequestBody PatientDtos.AllergyCreateRequest req) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.addAllergy(userId, req));
    }

    @GetMapping("/allergies")
    public ResponseEntity<List<PatientDtos.AllergyResponse>> listAllergies(@PathVariable UUID userId) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.listAllergies(userId));
    }

    // Medications
    @PostMapping("/medications")
    public ResponseEntity<PatientDtos.MedicationResponse> addMedication(@PathVariable UUID userId,
                                                                        @Valid @RequestBody PatientDtos.MedicationCreateRequest req) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.addMedication(userId, req));
    }

    @GetMapping("/medications")
    public ResponseEntity<List<PatientDtos.MedicationResponse>> listMedications(@PathVariable UUID userId) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.listMedications(userId));
    }

    // Vitals
    @PostMapping("/vitals")
    public ResponseEntity<PatientDtos.VitalResponse> addVital(@PathVariable UUID userId,
                                                              @Valid @RequestBody PatientDtos.VitalCreateRequest req) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.addVital(userId, req));
    }

    @GetMapping("/vitals")
    public ResponseEntity<List<PatientDtos.VitalResponse>> listVitals(@PathVariable UUID userId) {
        securityHelper.validatePatientAccess(userId);
        return ResponseEntity.ok(service.listVitals(userId));
    }
}
