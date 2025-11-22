package com.rehabai.plan_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rehabai.plan_service.config.RabbitConfig;
import com.rehabai.plan_service.dto.CreatePlanRequest;
import com.rehabai.plan_service.dto.PlanResponse;
import com.rehabai.plan_service.dto.UpdatePlanRequest;
import com.rehabai.plan_service.dto.SetActiveVersionResponse;
import com.rehabai.plan_service.dto.PlanAuditLogResponse;
import com.rehabai.plan_service.events.PlanApprovedEvent;
import com.rehabai.plan_service.integration.UserClient;
import com.rehabai.plan_service.model.Plan;
import com.rehabai.plan_service.model.PlanAuditLog;
import com.rehabai.plan_service.model.PlanOrigin;
import com.rehabai.plan_service.model.PlanPriority;
import com.rehabai.plan_service.model.PlanStatus;
import com.rehabai.plan_service.repository.PlanAuditLogRepository;
import com.rehabai.plan_service.repository.PlanRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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
    private final RabbitTemplate rabbitTemplate;
    private final com.rehabai.plan_service.security.SecurityHelper securityHelper;

    public PlanService(PlanRepository planRepo,
                       PlanAuditLogRepository auditRepo,
                       MeterRegistry meterRegistry,
                       ObjectMapper objectMapper,
                       UserClient userClient,
                       com.rehabai.plan_service.integration.PatientClient patientClient,
                       RabbitTemplate rabbitTemplate,
                       com.rehabai.plan_service.security.SecurityHelper securityHelper) {
        this.planRepo = planRepo;
        this.auditRepo = auditRepo;
        this.objectMapper = objectMapper;
        this.planCreated = meterRegistry.counter("plan.created");
        this.planUpdated = meterRegistry.counter("plan.updated");
        this.planApproved = meterRegistry.counter("plan.approved");
        this.userClient = userClient;
        this.patientClient = patientClient;
        this.rabbitTemplate = rabbitTemplate;
        this.securityHelper = securityHelper;
    }

    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request, UUID therapistId) {
        userClient.requireActivePatient(request.userId());
        patientClient.requirePatientProfile(request.userId());

        Integer maxVersion = planRepo.findMaxVersionByUserAndPrescription(
            request.userId(),
            request.prescriptionId()
        );
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;
        log.info("Request={}, nextVersion={}", request, nextVersion);
        Plan plan = new Plan();
        plan.setUserId(request.userId());
        plan.setPrescriptionId(request.prescriptionId());
        plan.setTherapistId(therapistId);
        plan.setVersion(nextVersion);
        plan.setOrigin(request.origin() != null ? request.origin() : PlanOrigin.AI_GENERATED);
        plan.setPriority(request.priority() != null ? request.priority() : PlanPriority.MEDIUM);
        plan.setConfidenceScore(request.confidenceScore());
        plan.setPainLevelStart(request.painLevelStart());
        plan.setPainLevelExpectedEnd(request.painLevelExpectedEnd());
        plan.setStartDate(request.startDate());
        plan.setEndDate(request.endDate());
        plan.setTags(request.tags() == null ? null : new java.util.ArrayList<>(request.tags()));
        plan.setActive(request.active() == null ? true : request.active());
        plan.setPlanData(parsePlanData(request.planData()));
        plan.setStatus(PlanStatus.DRAFT);

        plan = planRepo.save(plan);
        planCreated.increment();

        logAudit(plan.getId(), therapistId, "Plan created", null);

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

        JsonNode oldData = plan.getPlanData();
        if (request.planData() != null) {
            plan.setPlanData(parsePlanData(request.planData()));
        }

        if (request.origin() != null) {
            plan.setOrigin(request.origin());
        }
        if (request.priority() != null) {
            plan.setPriority(request.priority());
        }
        if (request.confidenceScore() != null) {
            plan.setConfidenceScore(request.confidenceScore());
        }
        if (request.painLevelStart() != null) {
            plan.setPainLevelStart(request.painLevelStart());
        }
        if (request.painLevelExpectedEnd() != null) {
            plan.setPainLevelExpectedEnd(request.painLevelExpectedEnd());
        }
        if (request.startDate() != null) {
            plan.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            plan.setEndDate(request.endDate());
        }
        if (request.tags() != null) {
            plan.setTags(new java.util.ArrayList<>(request.tags()));
        }
        if (request.active() != null) {
            plan.setActive(request.active());
        }

        if (request.status() != null && request.status() != plan.getStatus()) {
            enforceTransition(plan.getStatus(), request.status());
            plan.setStatus(request.status());
            if (request.status() == PlanStatus.APPROVED) {
                planApproved.increment();
            }
        }

        plan.setUpdateReason(request.reason());
        plan = planRepo.save(plan);
        planUpdated.increment();

        String diff = calculateDiff(oldData, plan.getPlanData());
        logAudit(plan.getId(), changedBy, request.reason(), diff);

        log.info("Plan updated: id={}, status={}", plan.getId(), plan.getStatus());

        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlan(UUID planId) {
        Plan plan = planRepo.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getPlansByUser(UUID userId) {
        if (securityHelper.isPatient()) {
            return planRepo.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        }
        return planRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getPlansByUserAndStatus(UUID userId, PlanStatus status) {
        if (securityHelper.isPatient()) {
            return planRepo.findByUserIdAndStatusAndActiveTrueOrderByCreatedAtDesc(userId, status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        }
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
    public List<PlanAuditLogResponse> getAuditHistory(UUID planId) {
        return auditRepo.findByPlanIdOrderByTimestampDesc(planId).stream()
            .map(this::toAuditResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public PlanResponse approvePlan(UUID planId, UUID approvedBy) {
        Plan plan = planRepo.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        enforceTransition(plan.getStatus(), PlanStatus.APPROVED);
        plan.setStatus(PlanStatus.APPROVED);
        plan.setUpdateReason("Plan approved");
        planRepo.deactivateOtherVersions(plan.getPrescriptionId(), planId);
        plan.setActive(true);
        plan = planRepo.save(plan);
        planUpdated.increment();
        planApproved.increment();

        logAudit(plan.getId(), approvedBy, "Plan approved", null);

        publishPlanApprovedEvent(plan, approvedBy);

        log.info("Plan approved: id={}, userId={}, approvedBy={}", plan.getId(), plan.getUserId(), approvedBy);

        return toResponse(plan);
    }

    @Transactional
    public PlanResponse archivePlan(UUID planId, UUID archivedBy, String reason) {
        return changeStatus(planId, archivedBy, PlanStatus.ARCHIVED, reason);
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
        clone.setTherapistId(base.getTherapistId());
        clone.setOrigin(base.getOrigin());
        clone.setConfidenceScore(base.getConfidenceScore());
        clone.setPriority(base.getPriority());
        clone.setPainLevelStart(base.getPainLevelStart());
        clone.setPainLevelExpectedEnd(base.getPainLevelExpectedEnd());
        clone.setStartDate(base.getStartDate());
        clone.setEndDate(base.getEndDate());
        clone.setTags(base.getTags() == null ? null : List.copyOf(base.getTags()));
        clone.setActive(base.isActive());
        clone.setPlanData(base.getPlanData());
        clone.setVersion(nextVersion);
        clone.setStatus(PlanStatus.DRAFT);
        clone = planRepo.save(clone);
        logAudit(clone.getId(), changedBy, reason != null ? reason : "New version created", null);
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
        clone.setTherapistId(current.getTherapistId());
        clone.setOrigin(target.getOrigin());
        clone.setConfidenceScore(target.getConfidenceScore());
        clone.setPriority(target.getPriority());
        clone.setPainLevelStart(target.getPainLevelStart());
        clone.setPainLevelExpectedEnd(target.getPainLevelExpectedEnd());
        clone.setStartDate(target.getStartDate());
        clone.setEndDate(target.getEndDate());
        clone.setTags(target.getTags() == null ? null : List.copyOf(target.getTags()));
        clone.setActive(target.isActive());
        clone.setPlanData(target.getPlanData());
        clone.setVersion(nextVersion);
        clone.setStatus(PlanStatus.DRAFT);
        clone = planRepo.save(clone);
        logAudit(clone.getId(), changedBy, reason != null ? reason : ("Rollback to version " + toVersion), null);
        return toResponse(clone);
    }

    private PlanResponse changeStatus(UUID planId, UUID changedBy, PlanStatus newStatus, String reason) {
        Plan plan = planRepo.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        enforceTransition(plan.getStatus(), newStatus);
        plan.setStatus(newStatus);
        plan.setUpdateReason(reason);
        plan = planRepo.save(plan);
        planUpdated.increment();
        logAudit(plan.getId(), changedBy, reason, null);
        if (newStatus == PlanStatus.APPROVED) {
            planApproved.increment();
        }
        return toResponse(plan);
    }

    private void enforceTransition(PlanStatus current, PlanStatus next) {
        if (current == PlanStatus.DRAFT && (next == PlanStatus.APPROVED || next == PlanStatus.ARCHIVED)) return;
        if (current == PlanStatus.APPROVED && next == PlanStatus.ARCHIVED) return;
        if (current == next) return;
        throw new IllegalStateException("invalid_status_transition:" + current + "->" + next);
    }

    private void logAudit(UUID planId, UUID changedBy, String reason, String diff) {
        PlanAuditLog logEntry = new PlanAuditLog();
        logEntry.setPlanId(planId);
        logEntry.setChangedBy(changedBy);
        logEntry.setReason(reason != null && !reason.isBlank() ? reason : "No reason provided");

        if (diff != null && !diff.equals("{}") && !diff.isBlank()) {
            logEntry.setChangeDiff(diff);
        } else {
            logEntry.setChangeDiff(null);
        }

        auditRepo.save(logEntry);
    }

    private String calculateDiff(JsonNode oldData, JsonNode newData) {
        try {
            if (oldData == null && newData == null) {
                return null;
            }
            if (oldData != null && oldData.equals(newData)) {
                return null;
            }

            var diffObject = objectMapper.createObjectNode();

            if (oldData != null) {
                diffObject.set("before", oldData);
            }
            if (newData != null) {
                diffObject.set("after", newData);
            }

            if (oldData != null && newData != null) {
                var changes = objectMapper.createArrayNode();

                oldData.fieldNames().forEachRemaining(fieldName -> {
                    JsonNode oldValue = oldData.get(fieldName);
                    JsonNode newValue = newData.get(fieldName);

                    if (newValue == null) {
                        var change = objectMapper.createObjectNode();
                        change.put("field", fieldName);
                        change.put("action", "removed");
                        changes.add(change);
                    } else if (!oldValue.equals(newValue)) {
                        var change = objectMapper.createObjectNode();
                        change.put("field", fieldName);
                        change.put("action", "modified");
                        changes.add(change);
                    }
                });

                newData.fieldNames().forEachRemaining(fieldName -> {
                    if (!oldData.has(fieldName)) {
                        var change = objectMapper.createObjectNode();
                        change.put("field", fieldName);
                        change.put("action", "added");
                        changes.add(change);
                    }
                });

                if (!changes.isEmpty()) {
                    diffObject.set("changes", changes);
                }
            }

            return objectMapper.writeValueAsString(diffObject);
        } catch (Exception e) {
            log.warn("Failed to calculate diff: {}", e.getMessage());
            return null;
        }
    }

    private PlanResponse toResponse(Plan plan) {
        return new PlanResponse(
            plan.getId(),
            plan.getUserId(),
            plan.getPrescriptionId(),
            plan.getTherapistId(),
            plan.getOrigin(),
            plan.getConfidenceScore(),
            plan.getVersion(),
            plan.getStatus(),
            planDataToString(plan.getPlanData()),
            plan.getPriority(),
            plan.getPainLevelStart(),
            plan.getPainLevelExpectedEnd(),
            plan.getStartDate(),
            plan.getEndDate(),
            plan.getTags(),
            plan.isActive(),
            plan.getUpdateReason(),
            plan.getPublishedAt(),
            plan.getPublishedBy(),
            plan.getReviewedAt(),
            plan.getReviewedBy(),
            plan.getCreatedAt(),
            plan.getUpdatedAt()
        );
    }

    private JsonNode parsePlanData(String planData) {
        try {
            return objectMapper.readTree(planData);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid planData JSON", e);
        }
    }

    private String planDataToString(JsonNode planData) {
        return planData == null ? null : planData.toString();
    }

    private void publishPlanApprovedEvent(Plan plan, UUID approvedBy) {
        try {
            String title = "Plano de Reabilitação";
            String description = "";

            if (plan.getPlanData() != null) {
                JsonNode planDataNode = plan.getPlanData();
                if (planDataNode.has("title")) {
                    title = planDataNode.get("title").asText(title);
                }
                if (planDataNode.has("description")) {
                    description = planDataNode.get("description").asText("");
                }
            }

            String userEmail = null;
            try {
                var userInfo = userClient.getUser(plan.getUserId());
                userEmail = userInfo.email();
            } catch (Exception e) {
                log.warn("Failed to fetch user email for userId={}: {}", plan.getUserId(), e.getMessage());
            }

            PlanApprovedEvent event = new PlanApprovedEvent(
                plan.getId(),
                plan.getUserId(),
                userEmail,
                plan.getPrescriptionId(),
                plan.getTherapistId(),
                approvedBy,
                OffsetDateTime.now(),
                title,
                description
            );

            rabbitTemplate.convertAndSend(
                RabbitConfig.PLAN_EVENTS_EXCHANGE,
                RabbitConfig.PLAN_APPROVED_ROUTING_KEY,
                event
            );

            log.info("Published PlanApprovedEvent: planId={}, userId={}, email={}",
                plan.getId(), plan.getUserId(), userEmail != null ? userEmail : "N/A");
        } catch (Exception e) {
            log.error("Failed to publish PlanApprovedEvent for plan {}: {}", plan.getId(), e.getMessage(), e);
        }
    }

    @Transactional
    public SetActiveVersionResponse setActiveVersion(UUID planId, UUID changedBy) {
        Plan targetPlan = planRepo.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));

        if (targetPlan.isActive()) {
            log.info("Plan {} (version {}) is already active", planId, targetPlan.getVersion());
            return SetActiveVersionResponse.alreadyActive(planId, targetPlan.getVersion());
        }

        int deactivatedCount = planRepo.deactivateOtherVersions(
            targetPlan.getPrescriptionId(),
            planId
        );

        targetPlan.setActive(true);
        targetPlan = planRepo.save(targetPlan);

        String reason = String.format("Set version %d as active (deactivated %d other version(s))",
            targetPlan.getVersion(), deactivatedCount);
        logAudit(planId, changedBy, reason, null);

        log.info("Plan {} (version {}) set as active. Deactivated {} other version(s)",
            planId, targetPlan.getVersion(), deactivatedCount);

        return SetActiveVersionResponse.activated(planId, targetPlan.getVersion(), deactivatedCount);
    }

    private PlanAuditLogResponse toAuditResponse(PlanAuditLog auditLog) {
        return new PlanAuditLogResponse(
            auditLog.getId(),
            auditLog.getPlanId(),
            auditLog.getChangedBy(),
            auditLog.getChangeDiff(),
            auditLog.getReason(),
            auditLog.getTimestamp()
        );
    }
}
