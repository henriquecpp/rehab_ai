package com.rehabai.prescription_service.controller;

import com.rehabai.prescription_service.dto.PlanDraftResponse;
import com.rehabai.prescription_service.dto.PrescriptionResponse;
import com.rehabai.prescription_service.integration.FileServiceClient;
import com.rehabai.prescription_service.security.SecurityHelper;
import com.rehabai.prescription_service.model.*;
import com.rehabai.prescription_service.repository.*;
import com.rehabai.prescription_service.service.PlanDraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescription Workflow", description = "Gerenciamento de workflows de prescrição com IA (OCR + Bedrock)")
public class WorkflowController {

    private final WorkflowRunRepository runRepo;
    private final ExtractionRepository extractionRepo;
    private final NormalizationRepository normalizationRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final AiTraceRepository aiTraceRepo;
    private final PlanDraftService planDraftService;
    private final SecurityHelper securityHelper;
    private final FileServiceClient fileServiceClient;

    @Operation(summary = "Buscar workflow mais recente", description = "🔒 CLINICIAN - Último workflow de um arquivo", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Workflow encontrado")
    @GetMapping("/workflows/latest")
    public ResponseEntity<?> latestWorkflow(@Parameter(description = "UUID do arquivo") @RequestParam UUID fileId) {
        securityHelper.requireClinician();
        return runRepo.findTopByFileIdOrderByCreatedAtDesc(fileId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar stages mais recentes", description = "🔒 CLINICIAN - Extraction, Normalization, Prescription", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Stages retornados")
    @GetMapping("/stages/latest")
    public ResponseEntity<?> latestStages(@Parameter(description = "UUID do arquivo") @RequestParam UUID fileId) {
        securityHelper.requireClinician();
        var extraction = extractionRepo.findTopByFileIdOrderByCreatedAtDesc(fileId).orElse(null);
        if (extraction == null) return ResponseEntity.notFound().build();
        var normalization = normalizationRepo.findTopByExtractionIdOrderByCreatedAtDesc(extraction.getId()).orElse(null);
        var prescription = normalization != null ? prescriptionRepo.findTopByNormalizationIdOrderByCreatedAtDesc(normalization.getId()).orElse(null) : null;
        return ResponseEntity.ok(Map.of(
                "extraction", extraction,
                "normalization", normalization,
                "prescription", prescription
        ));
    }

    @Operation(summary = "Buscar extraction por ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Extraction encontrada")
    @GetMapping("/extractions/{id}")
    public ResponseEntity<?> getExtraction(@Parameter(description = "UUID da extraction") @PathVariable UUID id) {
        securityHelper.requireClinician();
        return extractionRepo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar normalization por ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Normalization encontrada")
    @GetMapping("/normalizations/{id}")
    public ResponseEntity<?> getNormalization(@Parameter(description = "UUID da normalization") @PathVariable UUID id) {
        securityHelper.requireClinician();
        return normalizationRepo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar prescription por ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Prescription encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> getPrescriptionById(
            @Parameter(description = "UUID da prescription") @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String userRoles,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        securityHelper.requireClinician();
        return prescriptionRepo.findById(id)
                .map(prescription -> {
                    PrescriptionResponse response = new PrescriptionResponse();
                    response.setId(prescription.getId());
                    response.setFileId(prescription.getFileId());
                    response.setUserId(prescription.getUserId());
                    response.setNormalizationId(prescription.getNormalizationId());
                    response.setOriginalText(prescription.getPrescriptionText());
                    response.setJsonData(prescription.getParametersJson());
                    response.setCreatedAt(prescription.getCreatedAt());

                    if (prescription.getFileId() != null) {
                        try {
                            FileServiceClient.FileMetadataDTO fileMetadata = fileServiceClient.getFileMetadata(
                                    prescription.getFileId(), authToken, userId, userRoles, userEmail);

                            if (fileMetadata != null) {
                                PrescriptionResponse.FileMetadata fileMeta = new PrescriptionResponse.FileMetadata();
                                fileMeta.setFileId(fileMetadata.getId());
                                fileMeta.setFileName(fileMetadata.getOriginalName());
                                fileMeta.setFileType(fileMetadata.getFileType());
                                fileMeta.setViewUrl("/files/" + fileMetadata.getId() + "/view");
                                fileMeta.setDownloadUrl("/files/" + fileMetadata.getId() + "/download");
                                response.setFileMetadata(fileMeta);
                            }
                        } catch (Exception e) {
                            System.err.println("Erro ao buscar metadados do arquivo: " + e.getMessage());
                        }
                    }

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar prescription por ID (gerada)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Prescription encontrada")
    @GetMapping("/generated/{id}")
    public ResponseEntity<?> getPrescription(@Parameter(description = "UUID da prescription") @PathVariable UUID id) {
        securityHelper.requireClinician();
        return prescriptionRepo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Lista todas as prescrições geradas para um arquivo específico.
     * Útil para ver histórico de reprocessamentos.
     *
     * @param fileId ID do arquivo
     * @return Lista de prescrições ordenadas por data (mais recente primeiro)
     */
    @Operation(summary = "Listar prescrições por arquivo", description = "🔒 CLINICIAN - Histórico de prescrições de um arquivo", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Lista retornada")
    @GetMapping("/files/{fileId}/prescriptions")
    public ResponseEntity<List<Prescription>> listPrescriptionsByFile(@Parameter(description = "UUID do arquivo") @PathVariable UUID fileId) {
        securityHelper.requireClinician();
        List<Prescription> prescriptions = prescriptionRepo.findAllByFileId(fileId);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Lista todas as prescrições geradas para um paciente específico.
     * Mostra todo o histórico de prescrições processadas do paciente.
     *
     * @param userId ID do paciente
     * @return Lista de prescrições ordenadas por data (mais recente primeiro)
     */
    @Operation(summary = "Listar prescrições por paciente", description = "🔒 CLINICIAN - Histórico completo do paciente", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Lista retornada")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Prescription>> listPrescriptionsByUser(@Parameter(description = "UUID do paciente") @PathVariable UUID userId) {
        securityHelper.requireClinician();
        List<Prescription> prescriptions = prescriptionRepo.findAllByUserId(userId);
        return ResponseEntity.ok(prescriptions);
    }

    @Operation(summary = "Listar traces da IA", description = "🔒 ADMIN - Logs de debug do Bedrock", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "✅ Traces retornados")
    @GetMapping("/traces")
    public ResponseEntity<List<AiTrace>> listTraces(@Parameter(description = "Trace ID") @RequestParam String traceId) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(aiTraceRepo.findByTraceIdOrderByCreatedAtDesc(traceId));
    }

    /**
     * Gera um draft de plano de reabilitação a partir de uma prescrição processada.
     * Endpoint principal para ADMIN/CLINICIAN obter o plano gerado pela IA.
     *
     * @param prescriptionId ID da prescrição gerada
     * @param userId ID do usuário/paciente para quem o plano será criado
     * @return Draft estruturado do plano pronto para revisão e criação formal
     */
    @Operation(
        summary = "Gerar plan draft por prescription ID",
        description = "🔒 CLINICIAN - Gera plano de reabilitação (JSON) a partir da prescrição processada pela IA",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Plan draft gerado")
    @GetMapping("/generated/{prescriptionId}/plan-draft")
    public ResponseEntity<PlanDraftResponse> generatePlanDraft(
            @Parameter(description = "UUID da prescrição") @PathVariable UUID prescriptionId,
            @Parameter(description = "UUID do paciente") @RequestParam UUID userId) {
        securityHelper.requireClinician();

        PlanDraftResponse draft = planDraftService.generateDraft(prescriptionId, userId);
        return ResponseEntity.ok(draft);
    }

    /**
     * Obtém um draft de plano a partir do último workflow concluído para um arquivo.
     * Este é o endpoint mais conveniente para uso após o processamento de um laudo.
     *
     * @param fileId ID do arquivo do laudo médico
     * @param userId ID do usuário/paciente
     * @return Draft do plano ou 404 se não houver workflow concluído
     */
    @Operation(
        summary = "Gerar plan draft por file ID",
        description = "🔒 CLINICIAN - Pega o workflow mais recente completo e gera plano (mais conveniente)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Plan draft gerado")
    @GetMapping("/files/{fileId}/plan-draft")
    public ResponseEntity<PlanDraftResponse> getPlanDraftByFile(
            @Parameter(description = "UUID do arquivo") @PathVariable UUID fileId,
            @Parameter(description = "UUID do paciente") @RequestParam UUID userId) {
        securityHelper.requireClinician();

        WorkflowRun workflow = runRepo.findTopByFileIdOrderByCreatedAtDesc(fileId)
                .orElseThrow(() -> new IllegalArgumentException("No workflow found for file: " + fileId));

        if (workflow.getStatus() != WorkflowStatus.COMPLETED) {
            throw new IllegalStateException("Workflow not completed yet. Current status: " + workflow.getStatus());
        }

        Extraction extraction = extractionRepo.findTopByFileIdOrderByCreatedAtDesc(fileId)
                .orElseThrow(() -> new IllegalArgumentException("No extraction found for file: " + fileId));

        Normalization normalization = normalizationRepo.findTopByExtractionIdOrderByCreatedAtDesc(extraction.getId())
                .orElseThrow(() -> new IllegalArgumentException("No normalization found for extraction: " + extraction.getId()));

        Prescription prescription = prescriptionRepo.findTopByNormalizationIdOrderByCreatedAtDesc(normalization.getId())
                .orElseThrow(() -> new IllegalArgumentException("No prescription found for normalization: " + normalization.getId()));

        PlanDraftResponse draft = planDraftService.generateDraft(prescription.getId(), userId);
        return ResponseEntity.ok(draft);
    }
}
