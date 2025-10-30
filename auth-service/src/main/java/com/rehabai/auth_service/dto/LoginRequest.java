package com.rehabai.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login (imutável).
 * As validações são aplicadas nos parâmetros do record.
 */
public record LoginRequest(

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O formato do e-mail é inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}