package com.rehabai.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login (imutável).
 * As validações são aplicadas nos parâmetros do record.
 */
public record LoginRequest(

        @NotBlank(message = "O nome de usuário é obrigatório")
        String username,

        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}