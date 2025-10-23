package com.rehabai.auth_service.dto;

/**
 * DTO de resposta de autenticação (imutável).
 * Usa um construtor customizado para definir o tokenType padrão.
 */
public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn
) {

    public AuthResponse(String token, long expiresIn) {
        this(token, "Bearer", expiresIn);
    }
}