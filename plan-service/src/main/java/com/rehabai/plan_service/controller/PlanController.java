package com.rehabai.plan_service.controller;

import com.rehabai.plan_service.security.SecurityHelper;
import com.rehabai.plan_service.dto.CreatePlanRequest;
import com.rehabai.plan_service.dto.PlanResponse;
import com.rehabai.plan_service.dto.UpdatePlanRequest;
import com.rehabai.plan_service.model.PlanAuditLog;
import com.rehabai.plan_service.model.PlanStatus;
import com.rehabai.plan_service.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
@Tag(name = "Plan Management", description = "Gerenciamento de planos de reabilitação com versionamento e auditoria (JSONB)")
public class PlanController {

    private final PlanService planService;
    private final SecurityHelper securityHelper;

    @Operation(
        summary = "Criar plano",
        description = "🔒 CLINICIAN - Cria novo plano de reabilitação (planData em JSON)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "✅ Plano criado")
    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        securityHelper.requireClinician();
        PlanResponse response = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar plano por ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Plano encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> getPlan(@Parameter(description = "UUID do plano") @PathVariable UUID id) {
        PlanResponse response = planService.getPlan(id);
        securityHelper.validateResourceAccess(response.userId());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Atualizar plano",
        description = "🔒 CLINICIAN - Atualiza planData (cria nova versão automática)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Plano atualizado")
    @PutMapping("/{id}")
    public ResponseEntity<PlanResponse> updatePlan(
            @Parameter(description = "UUID do plano") @PathVariable UUID id,
            @Parameter(description = "UUID de quem alterou") @RequestParam(required = false) UUID changedBy,
            @Valid @RequestBody UpdatePlanRequest request) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.updatePlan(id, authenticatedUserId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar planos por paciente", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Lista retornada")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlanResponse>> getPlansByUser(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId) {
        securityHelper.validateResourceAccess(userId);
        List<PlanResponse> plans = planService.getPlansByUser(userId);
        return ResponseEntity.ok(plans);
    }

    @Operation(summary = "Listar planos por paciente e status", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Lista filtrada")
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<PlanResponse>> getPlansByUserAndStatus(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId,
            @Parameter(description = "Status do plano") @PathVariable PlanStatus status) {
        securityHelper.validateResourceAccess(userId);
        List<PlanResponse> plans = planService.getPlansByUserAndStatus(userId, status);
        return ResponseEntity.ok(plans);
    }

    @Operation(summary = "Listar todas as versões", description = "Histórico de versões de um plano", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Versões retornadas")
    @GetMapping("/prescription/{prescriptionId}/versions")
    public ResponseEntity<List<PlanResponse>> getPlanVersions(
            @Parameter(description = "UUID da prescription") @PathVariable UUID prescriptionId) {
        List<PlanResponse> versions = planService.getPlanVersions(prescriptionId);
        if (!versions.isEmpty()) {
            securityHelper.validateResourceAccess(versions.get(0).userId());
        }
        return ResponseEntity.ok(versions);
    }

    @Operation(summary = "Buscar versão mais recente", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Última versão")
    @GetMapping("/prescription/{prescriptionId}/latest")
    public ResponseEntity<PlanResponse> getLatestPlanVersion(
            @Parameter(description = "UUID da prescription") @PathVariable UUID prescriptionId) {
        PlanResponse response = planService.getLatestPlanVersion(prescriptionId);
        securityHelper.validateResourceAccess(response.userId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Histórico de auditoria", description = "Logs de todas as mudanças", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Logs retornados")
    @GetMapping("/{id}/audit")
    public ResponseEntity<List<PlanAuditLog>> getAuditHistory(
            @Parameter(description = "UUID do plano") @PathVariable UUID id) {
        PlanResponse plan = planService.getPlan(id);
        securityHelper.validateResourceAccess(plan.userId());

        List<PlanAuditLog> history = planService.getAuditHistory(id);
        return ResponseEntity.ok(history);
    }

    @Operation(
        summary = "Aprovar plano",
        description = "🔒 CLINICIAN - Muda status para APPROVED (read-only)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Plano aprovado")
    @PostMapping("/{id}/approve")
    public ResponseEntity<PlanResponse> approvePlan(
            @Parameter(description = "UUID do plano") @PathVariable UUID id,
            @Parameter(description = "UUID de quem aprovou") @RequestParam(required = false) UUID approvedBy) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.approvePlan(id, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Arquivar plano",
        description = "🔒 CLINICIAN - Muda status para ARCHIVED",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Plano arquivado")
    @PostMapping("/{id}/archive")
    public ResponseEntity<PlanResponse> archivePlan(
            @Parameter(description = "UUID do plano") @PathVariable UUID id,
            @Parameter(description = "UUID de quem arquivou") @RequestParam(required = false) UUID archivedBy,
            @Parameter(description = "Motivo do arquivamento") @RequestParam(required = false) String reason) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.archivePlan(id, authenticatedUserId, reason);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Criar nova versão",
        description = "🔒 CLINICIAN - Cria versão a partir de plano existente (incrementa versionNumber)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "✅ Nova versão criada")
    @PostMapping("/{id}/new-version")
    public ResponseEntity<PlanResponse> createNewVersion(
            @Parameter(description = "UUID do plano base") @PathVariable UUID id,
            @Parameter(description = "UUID de quem criou") @RequestParam(required = false) UUID changedBy,
            @Parameter(description = "Motivo da nova versão") @RequestParam(required = false) String reason) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.createNewVersion(id, authenticatedUserId, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Rollback para versão anterior",
        description = """
            # 🔒 **CLINICIAN** - Reverter para Versão Anterior
            
            Volta o plano para uma versão específica (cria nova versão com dados antigos).
            
            ## Como funciona:
            1. Busca a versão antiga especificada
            2. Cria nova versão (incrementa número)
            3. Copia planData da versão antiga
            4. Registra log de auditoria
            
            **Nota:** Não deleta versões, apenas cria nova baseada em antiga.
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "✅ Rollback realizado (nova versão criada)")
    @PostMapping("/{id}/rollback")
    public ResponseEntity<PlanResponse> rollback(
            @Parameter(description = "UUID do plano") @PathVariable UUID id,
            @Parameter(description = "Número da versão para voltar", example = "2", required = true) @RequestParam Integer toVersion,
            @Parameter(description = "UUID de quem fez rollback") @RequestParam(required = false) UUID changedBy,
            @Parameter(description = "Motivo do rollback") @RequestParam(required = false) String reason) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.rollbackToVersion(id, toVersion, authenticatedUserId, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
