package com.rehabai.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para renovar access token usando refresh token")
public record RefreshRequest(
        @Schema(
            description = "Refresh Token (UUID) recebido no login ou refresh anterior",
            example = "660e8400-e29b-41d4-a716-446655440000",
            required = true,
            format = "uuid"
        )
        @NotBlank
        String refreshToken
) {}

