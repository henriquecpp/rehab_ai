package com.rehabai.patient_service.controller;

import com.rehabai.patient_service.security.SecurityHelper;
import com.rehabai.patient_service.dto.PatientDtos;
import com.rehabai.patient_service.service.PatientProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/patients/{userId}/profile")
@RequiredArgsConstructor
@Tag(name = "Patient Profile", description = "Gerenciamento de perfis detalhados de pacientes")
public class PatientProfileController {

    private final PatientProfileService service;
    private final SecurityHelper securityHelper;

    @Operation(
        summary = "Buscar perfil do paciente",
        description = "Retorna perfil detalhado do paciente (idioma, sexo biológico, notas).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Perfil encontrado")
    @GetMapping
    public ResponseEntity<PatientDtos.ProfileResponse> get(
            @Parameter(description = "UUID do usuário/paciente") @PathVariable UUID userId) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.get(userId));
    }

    @Operation(
        summary = "Criar/Atualizar perfil",
        description = "Cria ou atualiza perfil do paciente (upsert).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Perfil salvo")
    @PutMapping
    public ResponseEntity<PatientDtos.ProfileResponse> upsert(
            @Parameter(description = "UUID do usuário/paciente") @PathVariable UUID userId,
            @Valid @RequestBody PatientDtos.ProfileRequest req) {
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.upsert(userId, req));
    }
}
