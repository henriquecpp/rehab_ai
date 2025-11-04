package com.rehabai.plan_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rehabai.plan_service.dto.CreatePlanRequest;
import com.rehabai.plan_service.dto.PlanResponse;
import com.rehabai.plan_service.dto.UpdatePlanRequest;
import com.rehabai.plan_service.integration.UserClient;
import com.rehabai.plan_service.model.Plan;
import com.rehabai.plan_service.model.PlanAuditLog;
import com.rehabai.plan_service.model.PlanStatus;
import com.rehabai.plan_service.repository.PlanAuditLogRepository;
import com.rehabai.plan_service.repository.PlanRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlanService {

    private static final Logger log = LoggerFactory.getLogger(PlanService.class);

    private final PlanRepository planRepo;
    private final PlanAuditLogRepository auditRepo;
    private final ObjectMapper objectMapper;
    private final Counter planCreated;
    private final Counter planUpdated;
    private final Counter planApproved;
    private final UserClient userClient;
    private final com.rehabai.plan_service.integration.PatientClient patientClient;

    public PlanService(PlanRepository planRepo,
                      PlanAuditLogRepository auditRepo,
                      MeterRegistry meterRegistry,
                      ObjectMapper objectMapper,
                      UserClient userClient,
                      com.rehabai.plan_service.integration.PatientClient patientClient) {
        this.planRepo = planRepo;
        this.auditRepo = auditRepo;
        this.objectMapper = objectMapper;
        this.planCreated = meterRegistry.counter("plan.created");
        this.planUpdated = meterRegistry.counter("plan.updated");
        this.planApproved = meterRegistry.counter("plan.approved");
        this.userClient = userClient;
        this.patientClient = patientClient;
    }

    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        userClient.requireActivePatient(request.userId());

        patientClient.requirePatientProfile(request.userId());

        Integer maxVersion = planRepo.findMaxVersionByUserAndPrescription(
            request.userId(),
            request.prescriptionId()
        );
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;

        Plan plan = new Plan();
        plan.setUserId(request.userId());
        plan.setPrescriptionId(request.prescriptionId());
        plan.setVersion(nextVersion);
        plan.setPlanData(request.planData());
        plan.setStatus(PlanStatus.DRAFT);

        plan = planRepo.save(plan);
        planCreated.increment();

        logAudit(plan.getId(), null, "Plan created", "{}");

        log.info("Plan created: id={}, userId={}, version={}",
            plan.getId(), plan.getUserId(), plan.getVersion());

        return toResponse(plan);
    }

    @Transactional
    public PlanResponse updatePlan(UUID planId, UUID changedBy, UpdatePlanRequest request) {
        Plan plan = planRepo.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));

        if (request.planData() != null && plan.getStatus() != PlanStatus.DRAFT) {
            throw new IllegalStateException("cannot_edit_non_draft");
        }

        String oldData = plan.getPlanData();
        if (request.planData() != null) {
            plan.setPlanData(request.planData());
        }

        if (request.status() != null && request.status() != plan.getStatus()) {
            enforceTransition(plan.getStatus(), request.status());
            plan.setStatus(request.status());
            if (request.status() == PlanStatus.APPROVED) {
                planApproved.increment();
            }
        }

        plan = planRepo.save(plan);
        planUpdated.increment();

        String diff = calculateDiff(oldData, plan.getPlanData());
        logAudit(plan.getId(), changedBy, request.reason(), diff);

        log.info("Plan updated: id={}, status={}", plan.getId(), plan.getStatus());

        return toResponse(plan);
    }

    private void enforceTransition(PlanStatus current, PlanStatus next) {
        if (current == PlanStatus.DRAFT && (next == PlanStatus.APPROVED || next == PlanStatus.ARCHIVED)) return;
        if (current == PlanStatus.APPROVED && next == PlanStatus.ARCHIVED) return;
        if (current == next) return;
        throw new IllegalStateException("invalid_status_transition:" + current + "->" + next);
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlan(UUID planId) {
        Plan plan = planRepo.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getPlansByUser(UUID userId) {
        return planRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getPlansByUserAndStatus(UUID userId, PlanStatus status) {
        return planRepo.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getPlanVersions(UUID prescriptionId) {
        return planRepo.findByPrescriptionIdOrderByVersionDesc(prescriptionId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanResponse getLatestPlanVersion(UUID prescriptionId) {
        Plan plan = planRepo.findTopByPrescriptionIdOrderByVersionDesc(prescriptionId)
            .orElseThrow(() -> new IllegalArgumentException("No plan found for prescription: " + prescriptionId));
        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanAuditLog> getAuditHistory(UUID planId) {
        return auditRepo.findByPlanIdOrderByTimestampDesc(planId);
    }

    @Transactional
    public PlanResponse approvePlan(UUID planId, UUID approvedBy) {
        UpdatePlanRequest request = new UpdatePlanRequest(null, PlanStatus.APPROVED, "Plan approved");
        return updatePlan(planId, approvedBy, request);
    }

    @Transactional
    public PlanResponse archivePlan(UUID planId, UUID archivedBy, String reason) {
        UpdatePlanRequest request = new UpdatePlanRequest(null, PlanStatus.ARCHIVED, reason);
        return updatePlan(planId, archivedBy, request);
    }

    @Transactional
    public PlanResponse createNewVersion(UUID basePlanId, UUID changedBy, String reason) {
        Plan base = planRepo.findById(basePlanId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + basePlanId));
        Integer maxVersion = planRepo.findMaxVersionByUserAndPrescription(base.getUserId(), base.getPrescriptionId());
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;
        Plan clone = new Plan();
        clone.setUserId(base.getUserId());
        clone.setPrescriptionId(base.getPrescriptionId());
        clone.setVersion(nextVersion);
        clone.setPlanData(base.getPlanData());
        clone.setStatus(PlanStatus.DRAFT);
        clone = planRepo.save(clone);
        logAudit(clone.getId(), changedBy, reason != null ? reason : "New version created", "{}");
        return toResponse(clone);
    }

    @Transactional
    public PlanResponse rollbackToVersion(UUID planId, Integer toVersion, UUID changedBy, String reason) {
        Plan current = planRepo.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        if (toVersion == null || toVersion < 1) {
            throw new IllegalArgumentException("invalid_target_version");
        }
        Plan target = planRepo.findByUserIdAndPrescriptionIdAndVersion(current.getUserId(), current.getPrescriptionId(), toVersion)
                .orElseThrow(() -> new IllegalArgumentException("target_version_not_found:" + toVersion));
        Integer maxVersion = planRepo.findMaxVersionByUserAndPrescription(current.getUserId(), current.getPrescriptionId());
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;
        Plan clone = new Plan();
        clone.setUserId(current.getUserId());
        clone.setPrescriptionId(current.getPrescriptionId());
        clone.setVersion(nextVersion);
        clone.setPlanData(target.getPlanData());
        clone.setStatus(PlanStatus.DRAFT);
        clone = planRepo.save(clone);
        logAudit(clone.getId(), changedBy, reason != null ? reason : ("Rollback to version " + toVersion), "{}");
        return toResponse(clone);
    }

    private void logAudit(UUID planId, UUID changedBy, String reason, String diff) {
        PlanAuditLog log = new PlanAuditLog();
        log.setPlanId(planId);
        log.setChangedBy(changedBy);
        log.setReason(reason);
        log.setChangeDiff(diff);
        auditRepo.save(log);
    }

    private String calculateDiff(String oldData, String newData) {
        try {
            JsonNode oldJson = objectMapper.readTree(oldData);
            JsonNode newJson = objectMapper.readTree(newData);
            return "{\"changed\": " + !oldJson.equals(newJson) + "}";
        } catch (Exception e) {
            log.warn("Failed to calculate diff: {}", e.getMessage());
            return "{\"error\": \"Could not calculate diff\"}";
        }
    }

    private PlanResponse toResponse(Plan plan) {
        return new PlanResponse(
            plan.getId(),
            plan.getUserId(),
            plan.getPrescriptionId(),
            plan.getVersion(),
            plan.getPlanData(),
            plan.getStatus(),
            plan.getCreatedAt(),
            plan.getUpdatedAt()
        );
    }
}
