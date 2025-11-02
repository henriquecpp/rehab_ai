package com.rehabai.plan_service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreatePlanRequest(
    @NotNull UUID userId,
    @NotNull UUID prescriptionId,
    @NotNull String planData
) {}

