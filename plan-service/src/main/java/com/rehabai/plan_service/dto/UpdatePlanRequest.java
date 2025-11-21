package com.rehabai.plan_service.dto;

import com.rehabai.plan_service.model.PlanOrigin;
import com.rehabai.plan_service.model.PlanPriority;
import com.rehabai.plan_service.model.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Requisição para atualizar plano (cria nova versão automaticamente)")
public record UpdatePlanRequest(
    @Schema(
        description = "Dados atualizados do plano em JSON",
        example = """
            {
              "title": "Plano de Reabilitação - Joelho (Revisado)",
              "diagnosis": "Gonartrose bilateral",
              "goals": ["Reduzir dor", "Melhorar mobilidade", "Fortalecer musculatura"],
              "exercises": [
                {
                  "name": "Alongamento quadríceps",
                  "sets": 3,
                  "reps": 20,
                  "frequency": "4x/semana"
                }
              ]
            }
            """,
        required = true
    )
    @NotNull String planData,

    @Schema(
        description = "Novo status do plano (opcional)",
        example = "PENDING_APPROVAL",
        allowableValues = {"DRAFT", "PENDING_APPROVAL", "APPROVED", "ARCHIVED"}
    )
    PlanStatus status,

    @Schema(description = "Motivo da atualização (para auditoria)", example = "Ajuste de frequência dos exercícios")
    String reason,

    @Schema(description = "Origem do plano", example = "AI_GENERATED", allowableValues = {"AI_GENERATED", "CLINICIAN_CREATED", "AI_REVIEWED_BY_THERAPIST"})
    PlanOrigin origin,

    @Schema(description = "Confiança da IA no plano gerado", example = "0.87")
    Double confidenceScore,

    @Schema(description = "Prioridade clínica", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    PlanPriority priority,

    @Schema(description = "Nível inicial de dor (0-10)", example = "6")
    Integer painLevelStart,

    @Schema(description = "Nível de dor esperado ao final do plano (0-10)", example = "2")
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
