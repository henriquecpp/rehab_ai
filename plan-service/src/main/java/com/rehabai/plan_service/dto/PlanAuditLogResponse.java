package com.rehabai.plan_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Resposta com dados de um registro de auditoria do plano")
public record PlanAuditLogResponse(
    @Schema(description = "UUID do registro de auditoria", example = "880e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "UUID do plano auditado", example = "770e8400-e29b-41d4-a716-446655440000")
    UUID planId,

    @Schema(description = "UUID do usuário que fez a alteração", example = "550e8400-e29b-41d4-a716-446655440000", nullable = true)
    UUID changedBy,

    @Schema(
        description = "Diferenças entre versões (JSON estruturado com before/after/changes)",
        example = """
            {
              "before": {
                "title": "Título Antigo",
                "exercises": [...]
              },
              "after": {
                "title": "Título Novo",
                "exercises": [...]
              },
              "changes": [
                {"field": "title", "action": "modified"},
                {"field": "exercises", "action": "modified"}
              ]
            }
            """,
        nullable = true
    )
    String changeDiff,

    @Schema(description = "Motivo da alteração", example = "Atualização dos exercícios conforme feedback do paciente")
    String reason,

    @Schema(description = "Data e hora da alteração", example = "2025-11-21T10:30:00Z")
    OffsetDateTime timestamp
) {
}

