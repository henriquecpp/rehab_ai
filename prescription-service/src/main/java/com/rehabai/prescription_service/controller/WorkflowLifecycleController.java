package com.rehabai.prescription_service.controller;

import com.rehabai.prescription_service.security.SecurityHelper;
import com.rehabai.prescription_service.model.WorkflowRun;
import com.rehabai.prescription_service.model.WorkflowStage;
import com.rehabai.prescription_service.model.WorkflowStatus;
import com.rehabai.prescription_service.repository.WorkflowRunRepository;
import com.rehabai.prescription_service.dto.StartRequest;
import com.rehabai.prescription_service.dto.AdvanceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/prescriptions/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflow Lifecycle", description = "Gerenciamento do ciclo de vida do workflow (start, advance, complete, fail, retry)")
public class WorkflowLifecycleController {

    private final WorkflowRunRepository runRepo;
    private final SecurityHelper securityHelper;

    @Operation(
        summary = "Iniciar workflow",
        description = "🔒 CLINICIAN - Cria novo workflow para processar arquivo com IA (OCR + Bedrock)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "✅ Workflow iniciado")
    @PostMapping
    public ResponseEntity<WorkflowRun> start(@RequestBody StartRequest req) {
        securityHelper.requireClinician();

        WorkflowRun run = new WorkflowRun();
        run.setUserId(req.userId());
        run.setFileId(req.fileId());
        run.setCurrentStage(WorkflowStage.EXTRACTION);
        run.setStatus(WorkflowStatus.RUNNING);
        run.setTraceId(req.traceId());
        WorkflowRun saved = runRepo.save(run);
        return ResponseEntity.created(URI.create("/prescriptions/workflows/" + saved.getId())).body(saved);
    }

    @Operation(
        summary = "Avançar stage do workflow",
        description = "🔒 CLINICIAN - Move workflow para próximo stage (EXTRACTION → NORMALIZATION → AI_GENERATION → DONE)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Stage avançado")
    @PostMapping("/{id}/advance")
    public ResponseEntity<?> advance(
            @Parameter(description = "UUID do workflow") @PathVariable UUID id,
            @RequestBody AdvanceRequest req) {
        securityHelper.requireClinician();

        WorkflowRun run = runRepo.findById(id).orElse(null);
        if (run == null) return ResponseEntity.notFound().build();
        if (run.getStatus() != WorkflowStatus.RUNNING) {
            return ResponseEntity.status(409).body(Map.of("error", "workflow_not_running"));
        }
        run.setCurrentStage(req.stage());
        if (req.stage() == WorkflowStage.DONE) {
            run.setStatus(WorkflowStatus.COMPLETED);
        }
        runRepo.save(run);
        return ResponseEntity.ok(run);
    }

    @Operation(
        summary = "Completar workflow",
        description = "🔒 CLINICIAN - Marca workflow como COMPLETED (DONE)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Workflow completado")
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@Parameter(description = "UUID do workflow") @PathVariable UUID id) {
        securityHelper.requireClinician();

        WorkflowRun run = runRepo.findById(id).orElse(null);
        if (run == null) return ResponseEntity.notFound().build();
        run.setCurrentStage(WorkflowStage.DONE);
        run.setStatus(WorkflowStatus.COMPLETED);
        runRepo.save(run);
        return ResponseEntity.ok(run);
    }

    @Operation(
        summary = "Marcar workflow como falho",
        description = "🔒 CLINICIAN - Workflow falhou (ERROR)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Workflow marcado como falho")
    @PostMapping("/{id}/fail")
    public ResponseEntity<?> fail(@Parameter(description = "UUID do workflow") @PathVariable UUID id) {
        securityHelper.requireClinician();

        WorkflowRun run = runRepo.findById(id).orElse(null);
        if (run == null) return ResponseEntity.notFound().build();
        run.setCurrentStage(WorkflowStage.ERROR);
        run.setStatus(WorkflowStatus.FAILED);
        runRepo.save(run);
        return ResponseEntity.ok(run);
    }

    @Operation(
        summary = "Retry workflow",
        description = "🔒 CLINICIAN - Reinicia workflow do início (volta para EXTRACTION)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Workflow reiniciado")
    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retry(@Parameter(description = "UUID do workflow") @PathVariable UUID id) {
        securityHelper.requireClinician();

        WorkflowRun run = runRepo.findById(id).orElse(null);
        if (run == null) return ResponseEntity.notFound().build();
        run.setCurrentStage(WorkflowStage.EXTRACTION);
        run.setStatus(WorkflowStatus.RUNNING);
        runRepo.save(run);
        return ResponseEntity.ok(run);
    }
}
