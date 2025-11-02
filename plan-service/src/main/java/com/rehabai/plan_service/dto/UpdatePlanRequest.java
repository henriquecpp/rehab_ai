package com.rehabai.plan_service.dto;

import com.rehabai.plan_service.model.PlanStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePlanRequest(
    @NotNull String planData,
    PlanStatus status,
    String reason
) {}

