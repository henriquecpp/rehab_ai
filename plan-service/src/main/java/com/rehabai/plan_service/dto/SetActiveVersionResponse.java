package com.rehabai.plan_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta ao definir versão ativa do plano")
public record SetActiveVersionResponse(
    @Schema(description = "UUID do plano que foi ativado", example = "880e8400-e29b-41d4-a716-446655440000")
    UUID activatedPlanId,

    @Schema(description = "Número da versão ativada", example = "3")
    Integer version,

    @Schema(description = "Indica se a versão já estava ativa", example = "false")
    boolean wasAlreadyActive,

    @Schema(description = "Número de versões que foram desativadas", example = "2")
    int deactivatedCount,

    @Schema(description = "Mensagem descritiva do resultado", example = "Version 3 activated successfully. 2 other versions were deactivated.")
    String message
) {
    public static SetActiveVersionResponse alreadyActive(UUID planId, Integer version) {
        return new SetActiveVersionResponse(
            planId,
            version,
            true,
            0,
            String.format("Version %d is already active", version)
        );
    }

    public static SetActiveVersionResponse activated(UUID planId, Integer version, int deactivatedCount) {
        return new SetActiveVersionResponse(
            planId,
            version,
            false,
            deactivatedCount,
            String.format("Version %d activated successfully. %d other version(s) were deactivated.", version, deactivatedCount)
        );
    }
}

