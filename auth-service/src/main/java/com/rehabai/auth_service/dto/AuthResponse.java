package com.rehabai.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta de autenticação contendo access token e refresh token.
 */
@Schema(description = "Resposta de autenticação com tokens JWT")
public record AuthResponse(
        @Schema(
            description = "Access Token JWT para autenticação em endpoints protegidos",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2FvQGV4YW1wbGUuY29tIiwidXNlcl9pZCI6IjU1MGU4NDAwLWUyOWItNDFkNC1hNzE2LTQ0NjY1NTQ0MDAwMCIsInJvbGVzIjpbIlJPTEVfUEFUSUVOVCJdLCJpYXQiOjE2OTk1MzYwMDAsImV4cCI6MTY5OTUzOTYwMH0.abc123",
            required = true
        )
        String token,

        @Schema(
            description = "Tipo de token (sempre Bearer)",
            example = "Bearer",
            defaultValue = "Bearer"
        )
        String tokenType,

        @Schema(
            description = "Tempo de expiração do access token em milissegundos",
            example = "3600000",
            implementation = Long.class
        )
        long expiresIn,

        @Schema(
            description = "Refresh Token (UUID) usado para renovar o access token quando expirar",
            example = "660e8400-e29b-41d4-a716-446655440000",
            format = "uuid"
        )
        String refreshToken,

        @Schema(
            description = "Tempo de expiração do refresh token em milissegundos",
            example = "604800000",
            implementation = Long.class
        )
        long refreshExpiresIn
) {

    public AuthResponse(String token, long expiresIn) {
        this(token, "Bearer", expiresIn, null, 0L);
    }

    public AuthResponse(String token, long expiresIn, String refreshToken, long refreshExpiresIn) {
        this(token, "Bearer", expiresIn, refreshToken, refreshExpiresIn);
    }
}