package com.rehabai.plan_service.dto;

import com.rehabai.plan_service.model.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

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
    String reason
) {}

