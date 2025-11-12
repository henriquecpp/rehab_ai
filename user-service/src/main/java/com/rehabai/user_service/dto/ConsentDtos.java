package com.rehabai.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ConsentDtos {
    @Schema(description = "Requisição para criar consentimento")
    public record CreateRequest(
            @Schema(
                description = "Tipo de consentimento",
                example = "data_processing",
                required = true,
                allowableValues = {"data_processing", "data_sharing", "marketing", "analytics"}
            )
            @NotBlank
            String type,

            @Schema(
                description = "Consentimento concedido (true) ou negado (false)",
                example = "true",
                required = true
            )
            @NotNull
            Boolean granted,

            @Schema(
                description = "Timestamp do consentimento (opcional, gerado automaticamente se omitido)",
                example = "2025-11-09T10:00:00Z"
            )
            OffsetDateTime timestamp
    ) {}

    @Schema(description = "Requisição para revogar consentimento")
    public record RevokeRequest(
            @Schema(
                description = "Tipo de consentimento a revogar",
                example = "marketing",
                required = true
            )
            @NotBlank
            String type
    ) {}

    @Schema(description = "Resposta com dados do consentimento")
    public record Response(
            @Schema(description = "UUID do consentimento", example = "770e8400-e29b-41d4-a716-446655440000")
            UUID id,

            @Schema(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID userId,

            @Schema(description = "Tipo de consentimento", example = "data_processing")
            String type,

            @Schema(description = "Consentimento concedido", example = "true")
            Boolean granted,

            @Schema(description = "Timestamp do consentimento", example = "2025-11-09T10:00:00Z")
            OffsetDateTime timestamp
    ) {}
}
