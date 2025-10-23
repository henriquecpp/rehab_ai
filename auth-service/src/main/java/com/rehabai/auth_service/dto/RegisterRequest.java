package com.rehabai.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de registro (imutável).
 * As validações são aplicadas nos parâmetros do record.
 */
public record RegisterRequest(

        @NotBlank(message = "O nome de usuário é obrigatório")
        @Size(min = 3, max = 100, message = "O nome de usuário deve ter entre 3 e 100 caracteres")
        String username,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O formato do e-mail é inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 128, message = "A senha deve ter entre 6 e 128 caracteres")
        String password
) {
}