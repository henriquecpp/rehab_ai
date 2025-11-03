package com.rehabai.plan_service.controller;

import com.rehabai.plan_service.security.SecurityHelper;
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
    private final SecurityHelper securityHelper;

    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        securityHelper.requireClinician();
        PlanResponse response = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable UUID id) {
        PlanResponse response = planService.getPlan(id);
        securityHelper.validateResourceAccess(response.userId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID changedBy,
            @Valid @RequestBody UpdatePlanRequest request) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.updatePlan(id, authenticatedUserId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlanResponse>> getPlansByUser(@PathVariable UUID userId) {
        securityHelper.validateResourceAccess(userId);
        List<PlanResponse> plans = planService.getPlansByUser(userId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<PlanResponse>> getPlansByUserAndStatus(
            @PathVariable UUID userId,
            @PathVariable PlanStatus status) {
        securityHelper.validateResourceAccess(userId);
        List<PlanResponse> plans = planService.getPlansByUserAndStatus(userId, status);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/prescription/{prescriptionId}/versions")
    public ResponseEntity<List<PlanResponse>> getPlanVersions(@PathVariable UUID prescriptionId) {
        List<PlanResponse> versions = planService.getPlanVersions(prescriptionId);
        if (!versions.isEmpty()) {
            securityHelper.validateResourceAccess(versions.get(0).userId());
        }
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/prescription/{prescriptionId}/latest")
    public ResponseEntity<PlanResponse> getLatestPlanVersion(@PathVariable UUID prescriptionId) {
        PlanResponse response = planService.getLatestPlanVersion(prescriptionId);
        securityHelper.validateResourceAccess(response.userId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<PlanAuditLog>> getAuditHistory(@PathVariable UUID id) {
        PlanResponse plan = planService.getPlan(id);
        securityHelper.validateResourceAccess(plan.userId());

        List<PlanAuditLog> history = planService.getAuditHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<PlanResponse> approvePlan(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID approvedBy) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.approvePlan(id, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<PlanResponse> archivePlan(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID archivedBy,
            @RequestParam(required = false) String reason) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.archivePlan(id, authenticatedUserId, reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/new-version")
    public ResponseEntity<PlanResponse> createNewVersion(@PathVariable UUID id,
                                                         @RequestParam(required = false) UUID changedBy,
                                                         @RequestParam(required = false) String reason) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.createNewVersion(id, authenticatedUserId, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/rollback")
    public ResponseEntity<PlanResponse> rollback(@PathVariable UUID id,
                                                 @RequestParam Integer toVersion,
                                                 @RequestParam(required = false) UUID changedBy,
                                                 @RequestParam(required = false) String reason) {
        securityHelper.requireClinician();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        PlanResponse response = planService.rollbackToVersion(id, toVersion, authenticatedUserId, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
