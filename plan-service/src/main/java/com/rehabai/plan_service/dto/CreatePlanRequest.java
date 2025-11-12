package com.rehabai.plan_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Requisição para criar plano de reabilitação")
public record CreatePlanRequest(
    @Schema(description = "UUID do paciente", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    @NotNull UUID userId,

    @Schema(description = "UUID da prescrição origem", example = "770e8400-e29b-41d4-a716-446655440000", required = true)
    @NotNull UUID prescriptionId,

    @Schema(
        description = "Dados do plano em formato JSON (JSONB no PostgreSQL)",
        example = """
            {
              "title": "Plano de Reabilitação - Joelho",
              "diagnosis": "Gonartrose",
              "goals": ["Reduzir dor", "Melhorar mobilidade"],
              "exercises": [
                {
                  "name": "Alongamento quadríceps",
                  "sets": 3,
                  "reps": 15,
                  "frequency": "3x/semana"
                }
              ],
              "duration": "8 semanas"
            }
            """,
        required = true
    )
    @NotNull String planData
) {}

