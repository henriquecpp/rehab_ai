package com.rehabai.plan_service.dto;

import com.rehabai.plan_service.model.PlanStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PlanResponse(
    UUID id,
    UUID userId,
    UUID prescriptionId,
    Integer version,
    String planData,
    PlanStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}

