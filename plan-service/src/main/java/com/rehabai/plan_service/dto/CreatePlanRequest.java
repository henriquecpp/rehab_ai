package com.rehabai.plan_service.dto;

import com.rehabai.plan_service.model.PlanOrigin;
import com.rehabai.plan_service.model.PlanPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Requisição para criar plano de reabilitação")
public record CreatePlanRequest(
    @Schema(description = "UUID do paciente", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    @NotNull(message = "userId is required")
    UUID userId,

    @Schema(description = "UUID da prescrição origem", example = "770e8400-e29b-41d4-a716-446655440000", required = true)
    @NotNull(message = "prescriptionId is required")
    UUID prescriptionId,

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
    @NotNull(message = "planData is required")
    @NotBlank(message = "planData cannot be blank")
    String planData,

    @Schema(description = "Origem do plano", example = "AI_GENERATED", allowableValues = {"AI_GENERATED", "MANUAL", "AI_REVIEWED_BY_THERAPIST"})
    PlanOrigin origin,

    @Schema(description = "Confiança da IA no plano gerado", example = "0.87")
    @DecimalMin(value = "0.0", message = "confidenceScore must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "confidenceScore must be between 0.0 and 1.0")
    Double confidenceScore,

    @Schema(description = "Prioridade clínica", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    PlanPriority priority,

    @Schema(description = "Nível inicial de dor (0-10)", example = "6")
    @Min(value = 0, message = "painLevelStart must be between 0 and 10")
    @Max(value = 10, message = "painLevelStart must be between 0 and 10")
    Integer painLevelStart,

    @Schema(description = "Nível de dor esperado ao final do plano (0-10)", example = "2")
    @Min(value = 0, message = "painLevelExpectedEnd must be between 0 and 10")
    @Max(value = 10, message = "painLevelExpectedEnd must be between 0 and 10")
    Integer painLevelExpectedEnd,

    @Schema(description = "Data prevista de início do plano", example = "2025-11-18T09:00:00Z")
    OffsetDateTime startDate,

    @Schema(description = "Data prevista de término do plano", example = "2025-12-30T09:00:00Z")
    OffsetDateTime endDate,

    @Schema(description = "Etiquetas clínicas e de workflow")
    List<String> tags,

    @Schema(description = "Indica se o plano está ativo", example = "true")
    Boolean active
) {}
