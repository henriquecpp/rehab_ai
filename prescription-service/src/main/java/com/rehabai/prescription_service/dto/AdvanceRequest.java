package com.rehabai.prescription_service.dto;

import com.rehabai.prescription_service.model.WorkflowStage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para avançar workflow para próximo stage")
public record AdvanceRequest(
    @Schema(
        description = "Próximo stage do workflow",
        example = "NORMALIZATION",
        required = true,
        allowableValues = {"EXTRACTION", "NORMALIZATION", "AI_GENERATION", "DONE", "ERROR"}
    )
    @NotNull WorkflowStage stage
) {}

