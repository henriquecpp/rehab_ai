package com.rehabai.prescription_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Requisição para iniciar workflow de prescrição")
public record StartRequest(
    @Schema(description = "UUID do paciente", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    @NotNull UUID userId,

    @Schema(description = "UUID do arquivo a processar", example = "660e8400-e29b-41d4-a716-446655440000", required = true)
    @NotNull UUID fileId,

    @Schema(description = "Trace ID para rastreamento (opcional)", example = "trace-123-abc")
    String traceId
) {}

