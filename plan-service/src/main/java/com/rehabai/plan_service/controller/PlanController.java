package com.rehabai.plan_service.controller;

import com.rehabai.plan_service.security.SecurityHelper;
import com.rehabai.plan_service.dto.CreatePlanRequest;
import com.rehabai.plan_service.dto.PlanResponse;
import com.rehabai.plan_service.dto.UpdatePlanRequest;
import com.rehabai.plan_service.dto.SetActiveVersionResponse;
import com.rehabai.plan_service.dto.PlanAuditLogResponse;
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
        UUID therapistId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.createPlan(request, therapistId);
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

    @Operation(
        summary = "Listar planos por paciente",
        description = """
            # Listar Planos por Paciente (com filtro por role)
            
            Retorna planos de um paciente com filtro baseado no role do usuário autenticado:
            
            ## Comportamento por Role:
            - **PATIENT (Paciente)**: Retorna apenas planos **ativos** (active=true)
            - **CLINICIAN/ADMIN**: Retorna **todos os planos** (ativos e inativos)
            
            ## Regras de Acesso:
            - Pacientes só podem ver seus próprios planos
            - Clinicians/Admins podem ver planos de qualquer paciente
            
            ## Ordenação:
            - Planos ordenados por data de criação (mais recente primeiro)
            
            **Nota:** Este comportamento complementa a funcionalidade de "set active version", 
            garantindo que pacientes vejam apenas a versão ativa do plano.
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Lista retornada (filtrada por role)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlanResponse>> getPlansByUser(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId) {
        securityHelper.validateResourceAccess(userId);
        List<PlanResponse> plans = planService.getPlansByUser(userId);
        return ResponseEntity.ok(plans);
    }

    @Operation(
        summary = "Listar planos por paciente e status",
        description = """
            # Listar Planos por Paciente e Status (com filtro por role)
            
            Retorna planos de um paciente filtrados por status, com filtro adicional baseado no role:
            
            ## Comportamento por Role:
            - **PATIENT (Paciente)**: Retorna apenas planos **ativos** (active=true) com o status especificado
            - **CLINICIAN/ADMIN**: Retorna **todos os planos** (ativos e inativos) com o status especificado
            
            ## Status Disponíveis:
            - DRAFT: Rascunho
            - APPROVED: Aprovado
            - ARCHIVED: Arquivado
            
            ## Regras de Acesso:
            - Pacientes só podem ver seus próprios planos
            - Clinicians/Admins podem ver planos de qualquer paciente
            
            ## Ordenação:
            - Planos ordenados por data de criação (mais recente primeiro)
            
            **Exemplo:** Um paciente buscando status=APPROVED verá apenas planos aprovados E ativos.
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Lista filtrada (por status e role)")
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<PlanResponse>> getPlansByUserAndStatus(
            @Parameter(description = "UUID do paciente") @PathVariable UUID userId,
            @Parameter(description = "Status do plano (DRAFT, APPROVED, ARCHIVED)") @PathVariable PlanStatus status) {
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

    @Operation(
        summary = "Histórico de auditoria",
        description = """
            # Histórico de Auditoria do Plano
            
            Retorna todos os registros de auditoria de um plano específico, ordenados por data (mais recente primeiro).
            
            ## Informações Retornadas:
            - **id**: UUID do registro de auditoria
            - **planId**: UUID do plano auditado
            - **changedBy**: UUID do usuário que fez a alteração (pode ser null para operações do sistema)
            - **changeDiff**: JSON estruturado com as diferenças (null para operações sem mudança de dados)
            - **reason**: Descrição da alteração
            - **timestamp**: Data e hora da alteração
            
            ## Formato do changeDiff:
            Para atualizações de dados, o campo `changeDiff` contém:
            ```json
            {
              "before": {...dados antigos...},
              "after": {...dados novos...},
              "changes": [
                {"field": "title", "action": "modified"},
                {"field": "exercises", "action": "modified"}
              ]
            }
            ```
            
            Para outras operações (criação, aprovação, etc.), `changeDiff` é null.
            
            ## Controle de Acesso:
            - Pacientes só podem ver auditoria dos próprios planos
            - Clinicians/Admins podem ver auditoria de qualquer plano
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
        responseCode = "200",
        description = "✅ Histórico de auditoria retornado com sucesso"
    )
    @GetMapping("/{id}/audit")
    public ResponseEntity<List<PlanAuditLogResponse>> getAuditHistory(
            @Parameter(description = "UUID do plano", example = "770e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        PlanResponse plan = planService.getPlan(id);
        securityHelper.validateResourceAccess(plan.userId());

        List<PlanAuditLogResponse> history = planService.getAuditHistory(id);
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

    @Operation(
        summary = "Definir versão ativa",
        description = """
            # 🔒 **CLINICIAN** - Definir Versão Ativa do Plano
            
            Define uma versão específica como ativa e desativa automaticamente todas as outras versões do mesmo plano.
            
            ## Como funciona:
            1. Verifica se a versão especificada já está ativa
            2. Se já ativa, retorna indicação sem alterações
            3. Se não, ativa a versão especificada
            4. Desativa todas as outras versões do mesmo prescriptionId
            5. Registra log de auditoria
            
            ## Retorna:
            - **wasAlreadyActive**: true se a versão já estava ativa
            - **deactivatedCount**: número de versões desativadas
            - **message**: descrição do resultado
            
            **Nota:** Apenas uma versão pode estar ativa por vez para cada prescription.
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Versão ativa definida com sucesso")
    @PostMapping("/{id}/set-active")
    public ResponseEntity<SetActiveVersionResponse> setActiveVersion(
            @Parameter(description = "UUID do plano a ser ativado") @PathVariable UUID id,
            @Parameter(description = "UUID de quem está alterando") @RequestParam(required = false) UUID changedBy) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        SetActiveVersionResponse response = planService.setActiveVersion(id, authenticatedUserId);
        return ResponseEntity.ok(response);
    }
}
