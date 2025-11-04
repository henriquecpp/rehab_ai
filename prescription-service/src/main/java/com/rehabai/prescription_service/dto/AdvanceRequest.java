package com.rehabai.prescription_service.dto;

import com.rehabai.prescription_service.model.WorkflowStage;
import jakarta.validation.constraints.NotNull;

public record AdvanceRequest(@NotNull WorkflowStage stage) {}

