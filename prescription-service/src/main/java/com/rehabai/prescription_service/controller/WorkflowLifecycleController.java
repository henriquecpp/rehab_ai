package com.rehabai.prescription_service.controller;

import com.rehabai.prescription_service.model.WorkflowRun;
import com.rehabai.prescription_service.model.WorkflowStage;
import com.rehabai.prescription_service.model.WorkflowStatus;
import com.rehabai.prescription_service.repository.WorkflowRunRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/prescriptions/workflows")
public class WorkflowLifecycleController {

    private final WorkflowRunRepository runRepo;

    public WorkflowLifecycleController(WorkflowRunRepository runRepo) {
        this.runRepo = runRepo;
    }

    public record StartRequest(@NotNull UUID userId, @NotNull UUID fileId, String traceId) {}

    @PostMapping
    public ResponseEntity<WorkflowRun> start(@RequestBody StartRequest req) {
        WorkflowRun run = new WorkflowRun();
        run.setUserId(req.userId());
        run.setFileId(req.fileId());
        run.setCurrentStage(WorkflowStage.EXTRACTION);
        run.setStatus(WorkflowStatus.RUNNING);
        run.setTraceId(req.traceId());
        WorkflowRun saved = runRepo.save(run);
        return ResponseEntity.created(URI.create("/prescriptions/workflows/" + saved.getId())).body(saved);
    }

    public record AdvanceRequest(@NotNull WorkflowStage stage) {}

    @PostMapping("/{id}/advance")
    public ResponseEntity<?> advance(@PathVariable UUID id, @RequestBody AdvanceRequest req) {
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

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable UUID id) {
        WorkflowRun run = runRepo.findById(id).orElse(null);
        if (run == null) return ResponseEntity.notFound().build();
        run.setCurrentStage(WorkflowStage.DONE);
        run.setStatus(WorkflowStatus.COMPLETED);
        runRepo.save(run);
        return ResponseEntity.ok(run);
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<?> fail(@PathVariable UUID id) {
        WorkflowRun run = runRepo.findById(id).orElse(null);
        if (run == null) return ResponseEntity.notFound().build();
        run.setCurrentStage(WorkflowStage.ERROR);
        run.setStatus(WorkflowStatus.FAILED);
        runRepo.save(run);
        return ResponseEntity.ok(run);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retry(@PathVariable UUID id) {
        WorkflowRun run = runRepo.findById(id).orElse(null);
        if (run == null) return ResponseEntity.notFound().build();
        run.setCurrentStage(WorkflowStage.EXTRACTION);
        run.setStatus(WorkflowStatus.RUNNING);
        runRepo.save(run);
        return ResponseEntity.ok(run);
    }
}

