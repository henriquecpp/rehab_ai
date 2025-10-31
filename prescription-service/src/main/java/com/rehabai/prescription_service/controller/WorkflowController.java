package com.rehabai.prescription_service.controller;

import com.rehabai.prescription_service.model.*;
import com.rehabai.prescription_service.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/prescriptions")
public class WorkflowController {

    private final WorkflowRunRepository runRepo;
    private final ExtractionRepository extractionRepo;
    private final NormalizationRepository normalizationRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final AiTraceRepository aiTraceRepo;

    public WorkflowController(WorkflowRunRepository runRepo,
                              ExtractionRepository extractionRepo,
                              NormalizationRepository normalizationRepo,
                              PrescriptionRepository prescriptionRepo,
                              AiTraceRepository aiTraceRepo) {
        this.runRepo = runRepo;
        this.extractionRepo = extractionRepo;
        this.normalizationRepo = normalizationRepo;
        this.prescriptionRepo = prescriptionRepo;
        this.aiTraceRepo = aiTraceRepo;
    }

    @GetMapping("/workflows/latest")
    public ResponseEntity<?> latestWorkflow(@RequestParam UUID fileId) {
        return runRepo.findTopByFileIdOrderByCreatedAtDesc(fileId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/stages/latest")
    public ResponseEntity<?> latestStages(@RequestParam UUID fileId) {
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

    @GetMapping("/extractions/{id}")
    public ResponseEntity<?> getExtraction(@PathVariable UUID id) {
        return extractionRepo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/normalizations/{id}")
    public ResponseEntity<?> getNormalization(@PathVariable UUID id) {
        return normalizationRepo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/generated/{id}")
    public ResponseEntity<?> getPrescription(@PathVariable UUID id) {
        return prescriptionRepo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/traces")
    public ResponseEntity<List<AiTrace>> listTraces(@RequestParam String traceId) {
        return ResponseEntity.ok(aiTraceRepo.findByTraceIdOrderByCreatedAtDesc(traceId));
    }
}
