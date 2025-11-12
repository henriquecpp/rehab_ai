package com.rehabai.patient_service.controller;

import com.rehabai.patient_service.security.SecurityHelper;
import com.rehabai.patient_service.dto.PatientDtos;
import com.rehabai.patient_service.service.PatientHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients/{userId}")
@RequiredArgsConstructor
@Tag(name = "Patient History", description = "Histórico clínico completo (notas, condições, alergias, medicações, sinais vitais)")
public class PatientHistoryController {

    private final PatientHistoryService service;
    private final SecurityHelper securityHelper;

    // Notes
    @Operation(summary = "Adicionar nota clínica", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Nota adicionada")
    @PostMapping("/history/notes")
    public ResponseEntity<PatientDtos.NoteResponse> addNote(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId,
            @Valid @RequestBody PatientDtos.NoteCreateRequest req) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.addNote(userId, req));
    }

    @Operation(summary = "Listar notas clínicas", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Lista de notas")
    @GetMapping("/history/notes")
    public ResponseEntity<List<PatientDtos.NoteResponse>> listNotes(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.listNotes(userId));
    }

    // Conditions
    @Operation(summary = "Adicionar condição médica", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Condição adicionada")
    @PostMapping("/conditions")
    public ResponseEntity<PatientDtos.ConditionResponse> addCondition(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId,
            @Valid @RequestBody PatientDtos.ConditionCreateRequest req) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.addCondition(userId, req));
    }

    @Operation(summary = "Listar condições médicas", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Lista de condições")
    @GetMapping("/conditions")
    public ResponseEntity<List<PatientDtos.ConditionResponse>> listConditions(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.listConditions(userId));
    }

    @Operation(summary = "Buscar condição por ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Condição encontrada")
    @GetMapping("/conditions/{conditionId}")
    public ResponseEntity<PatientDtos.ConditionResponse> getCondition(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId,
            @Parameter(description = "UUID da condição") @PathVariable UUID conditionId) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.getCondition(userId, conditionId));
    }

    @Operation(summary = "Deletar condição", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "✅ Condição deletada")
    @DeleteMapping("/conditions/{conditionId}")
    public ResponseEntity<Void> deleteCondition(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId,
            @Parameter(description = "UUID da condição") @PathVariable UUID conditionId) {
        securityHelper.validateResourceAccess(userId);
        service.deleteCondition(userId, conditionId);
        return ResponseEntity.noContent().build();
    }

    // Allergies
    @Operation(summary = "Adicionar alergia", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Alergia adicionada")
    @PostMapping("/allergies")
    public ResponseEntity<PatientDtos.AllergyResponse> addAllergy(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId,
            @Valid @RequestBody PatientDtos.AllergyCreateRequest req) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.addAllergy(userId, req));
    }

    @Operation(summary = "Listar alergias", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Lista de alergias")
    @GetMapping("/allergies")
    public ResponseEntity<List<PatientDtos.AllergyResponse>> listAllergies(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.listAllergies(userId));
    }

    // Medications
    @Operation(summary = "Adicionar medicação", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Medicação adicionada")
    @PostMapping("/medications")
    public ResponseEntity<PatientDtos.MedicationResponse> addMedication(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId,
            @Valid @RequestBody PatientDtos.MedicationCreateRequest req) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.addMedication(userId, req));
    }

    @Operation(summary = "Listar medicações", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Lista de medicações")
    @GetMapping("/medications")
    public ResponseEntity<List<PatientDtos.MedicationResponse>> listMedications(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.listMedications(userId));
    }

    // Vitals
    @Operation(summary = "Adicionar sinal vital", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Sinal vital adicionado")
    @PostMapping("/vitals")
    public ResponseEntity<PatientDtos.VitalResponse> addVital(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId,
            @Valid @RequestBody PatientDtos.VitalCreateRequest req) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.addVital(userId, req));
    }

    @Operation(summary = "Listar sinais vitais", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Lista de sinais vitais")
    @GetMapping("/vitals")
    public ResponseEntity<List<PatientDtos.VitalResponse>> listVitals(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.listVitals(userId));
    }
}
