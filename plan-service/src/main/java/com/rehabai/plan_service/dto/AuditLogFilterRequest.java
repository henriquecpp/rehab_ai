package com.rehabai.plan_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Filtros para buscar registros de auditoria")
public record AuditLogFilterRequest(
    @Schema(description = "UUID do plano para filtrar", example = "770e8400-e29b-41d4-a716-446655440000", nullable = true)
    UUID planId,

    @Schema(description = "UUID do usuário que fez alterações", example = "550e8400-e29b-41d4-a716-446655440000", nullable = true)
    UUID changedBy,

    @Schema(description = "Data de início do período (ISO 8601)", example = "2025-11-01T00:00:00Z", nullable = true)
    OffsetDateTime startDate,

    @Schema(description = "Data de fim do período (ISO 8601)", example = "2025-11-21T23:59:59Z", nullable = true)
    OffsetDateTime endDate,

    @Schema(description = "Filtrar apenas logs com diferenças (change_diff não nulo)", example = "false", nullable = true, defaultValue = "false")
    Boolean onlyWithDiff,

    @Schema(description = "Número da página (iniciando em 0)", example = "0", nullable = true, defaultValue = "0")
    Integer page,

    @Schema(description = "Tamanho da página", example = "20", nullable = true, defaultValue = "20")
    Integer size
) {
    public AuditLogFilterRequest {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size < 1) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }
        if (onlyWithDiff == null) {
            onlyWithDiff = false;
        }
    }
}

