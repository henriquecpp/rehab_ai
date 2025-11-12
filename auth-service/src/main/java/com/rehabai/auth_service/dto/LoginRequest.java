package com.rehabai.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login.
 */
@Schema(description = "Credenciais de login do usuário")
public record LoginRequest(

        @Schema(
            description = "Email cadastrado no sistema",
            example = "joao.silva@example.com",
            required = true,
            format = "email"
        )
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O formato do e-mail é inválido")
        String email,

        @Schema(
            description = "Senha do usuário",
            example = "senha123",
            required = true,
            format = "password"
        )
        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}