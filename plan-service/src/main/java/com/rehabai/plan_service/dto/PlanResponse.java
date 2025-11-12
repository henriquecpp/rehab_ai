package com.rehabai.plan_service.dto;

import com.rehabai.plan_service.model.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Resposta com dados do plano de reabilitação")
public record PlanResponse(
    @Schema(description = "UUID do plano", example = "880e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "UUID do paciente", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID userId,

    @Schema(description = "UUID da prescrição origem", example = "770e8400-e29b-41d4-a716-446655440000")
    UUID prescriptionId,

    @Schema(description = "Número da versão (incrementa a cada update)", example = "3")
    Integer version,

    @Schema(
        description = "Dados do plano em JSON (JSONB)",
        example = """
            {
              "title": "Plano de Reabilitação - Joelho",
              "diagnosis": "Gonartrose",
              "goals": ["Reduzir dor", "Melhorar mobilidade"],
              "exercises": [...]
            }
            """
    )
    String planData,

    @Schema(description = "Status atual do plano", example = "APPROVED")
    PlanStatus status,

    @Schema(description = "Data de criação", example = "2025-11-09T10:00:00Z")
    OffsetDateTime createdAt,

    @Schema(description = "Data da última atualização", example = "2025-11-09T15:30:00Z")
    OffsetDateTime updatedAt
) {}

