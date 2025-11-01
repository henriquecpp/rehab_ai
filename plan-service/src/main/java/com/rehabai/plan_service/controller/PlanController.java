package com.rehabai.plan_service.controller;

import com.rehabai.plan_service.dto.CreatePlanRequest;
import com.rehabai.plan_service.dto.PlanResponse;
import com.rehabai.plan_service.dto.UpdatePlanRequest;
import com.rehabai.plan_service.model.PlanAuditLog;
import com.rehabai.plan_service.model.PlanStatus;
import com.rehabai.plan_service.service.PlanService;
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
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        PlanResponse response = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable UUID id) {
        PlanResponse response = planService.getPlan(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID changedBy,
            @Valid @RequestBody UpdatePlanRequest request) {
        PlanResponse response = planService.updatePlan(id, changedBy, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlanResponse>> getPlansByUser(@PathVariable UUID userId) {
        List<PlanResponse> plans = planService.getPlansByUser(userId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<PlanResponse>> getPlansByUserAndStatus(
            @PathVariable UUID userId,
            @PathVariable PlanStatus status) {
        List<PlanResponse> plans = planService.getPlansByUserAndStatus(userId, status);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/prescription/{prescriptionId}/versions")
    public ResponseEntity<List<PlanResponse>> getPlanVersions(@PathVariable UUID prescriptionId) {
        List<PlanResponse> versions = planService.getPlanVersions(prescriptionId);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/prescription/{prescriptionId}/latest")
    public ResponseEntity<PlanResponse> getLatestPlanVersion(@PathVariable UUID prescriptionId) {
        PlanResponse response = planService.getLatestPlanVersion(prescriptionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<PlanAuditLog>> getAuditHistory(@PathVariable UUID id) {
        List<PlanAuditLog> history = planService.getAuditHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<PlanResponse> approvePlan(
            @PathVariable UUID id,
            @RequestParam UUID approvedBy) {
        PlanResponse response = planService.approvePlan(id, approvedBy);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<PlanResponse> archivePlan(
            @PathVariable UUID id,
            @RequestParam UUID archivedBy,
            @RequestParam(required = false) String reason) {
        PlanResponse response = planService.archivePlan(id, archivedBy, reason);
        return ResponseEntity.ok(response);
    }

    // New versioning endpoints
    @PostMapping("/{id}/new-version")
    public ResponseEntity<PlanResponse> createNewVersion(@PathVariable UUID id,
                                                         @RequestParam(required = false) UUID changedBy,
                                                         @RequestParam(required = false) String reason) {
        PlanResponse response = planService.createNewVersion(id, changedBy, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/rollback")
    public ResponseEntity<PlanResponse> rollback(@PathVariable UUID id,
                                                 @RequestParam Integer toVersion,
                                                 @RequestParam(required = false) UUID changedBy,
                                                 @RequestParam(required = false) String reason) {
        PlanResponse response = planService.rollbackToVersion(id, toVersion, changedBy, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
