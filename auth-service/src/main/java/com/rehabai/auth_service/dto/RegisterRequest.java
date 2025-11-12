package com.rehabai.auth_service.dto;

import com.rehabai.auth_service.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de registro de novo usuário.
 */
@Schema(description = "Dados para registro de novo usuário no sistema")
public record RegisterRequest(

        @Schema(
            description = "Email do usuário (será usado para login)",
            example = "joao.silva@example.com",
            required = true,
            format = "email"
        )
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O formato do e-mail é inválido")
        String email,

        @Schema(
            description = "Senha do usuário (mínimo 6 caracteres)",
            example = "senha123",
            required = true,
            minLength = 6,
            maxLength = 128,
            format = "password"
        )
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 128, message = "A senha deve ter entre 6 e 128 caracteres")
        String password,

        @Schema(
            description = "Nome completo do usuário",
            example = "João Silva",
            required = true
        )
        String fullName,

        @Schema(
            description = """
                Role/Perfil do usuário no sistema:
                - PATIENT: Paciente
                - CLINICIAN: Profissional de saúde (fisioterapeuta, médico)
                - ADMIN: Administrador do sistema (requer permissão especial)
                """,
            example = "PATIENT",
            required = true,
            allowableValues = {"PATIENT", "CLINICIAN", "ADMIN"}
        )
        UserRole role
) {
}