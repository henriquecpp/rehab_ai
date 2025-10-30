package com.rehabai.auth_service.dto;

/**




2301 * DTO de resposta de autenticação (imutável), incluindo refresh token.
 */
public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn
) {

    public AuthResponse(String token, long expiresIn) {
        this(token, "Bearer", expiresIn, null, 0L);
    }

    public AuthResponse(String token, long expiresIn, String refreshToken, long refreshExpiresIn) {
        this(token, "Bearer", expiresIn, refreshToken, refreshExpiresIn);
    }
}