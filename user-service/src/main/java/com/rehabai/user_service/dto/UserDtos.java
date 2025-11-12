package com.rehabai.user_service.dto;

import com.rehabai.user_service.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class UserDtos {

    @Schema(description = "Requisição para criar novo usuário")
    public record CreateRequest(
        @Schema(
            description = "Email do usuário (único no sistema)",
            example = "maria.santos@example.com",
            required = true
        )
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @Schema(
            description = "Nome completo do usuário",
            example = "Maria Santos",
            required = true
        )
        @NotBlank(message = "Nome completo é obrigatório")
        String fullName,

        @Schema(
            description = "Hash BCrypt da senha (não enviar senha em texto puro)",
            example = "$2a$10$N9qo8uLOickgx2ZMRZoMye/7VgMHv.XCwPPMz.PGXzZ6zb4iVmZqK",
            required = true
        )
        @NotBlank(message = "passwordHash é obrigatório (hash de senha)")
        String passwordHash,

        @Schema(
            description = "Role/Perfil do usuário no sistema",
            example = "PATIENT",
            allowableValues = {"PATIENT", "CLINICIAN", "ADMIN"}
        )
        UserRole role
    ) {}

    @Schema(description = "Requisição para atualizar dados do usuário")
    public record UpdateRequest(
        @Schema(description = "Novo nome completo", example = "Maria Santos Silva")
        String fullName,

        @Schema(
            description = "Nova role",
            allowableValues = {"PATIENT", "CLINICIAN", "ADMIN"}
        )
        UserRole role,

        @Schema(description = "Status ativo (true = pode fazer login)", example = "true")
        Boolean active
    ) {}

    @Schema(description = "Requisição para trocar senha do usuário")
    public record ChangePasswordRequest(
        @Schema(
            description = "Novo hash BCrypt da senha",
            example = "$2a$10$NewHashHere...",
            required = true
        )
        @NotBlank
        String passwordHash
    ) {}

    @Schema(description = "Requisição para trocar role do usuário")
    public record ChangeRoleRequest(
        @Schema(
            description = "Nova role",
            example = "CLINICIAN",
            required = true,
            allowableValues = {"PATIENT", "CLINICIAN", "ADMIN"}
        )
        @NotNull
        UserRole role
    ) {}

    @Schema(description = "Resposta com dados do usuário (sem senha)")
    public record Response(
        @Schema(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Email do usuário", example = "maria@example.com")
        String email,

        @Schema(description = "Nome completo", example = "Maria Santos")
        String fullName,

        @Schema(description = "Role/Perfil", example = "PATIENT")
        UserRole role,

        @Schema(description = "Status ativo", example = "true")
        Boolean active
    ) {}

    @Schema(description = "🔒 INTERNO - Resposta com credenciais completas (inclui passwordHash)")
    public record CredentialsResponse(
        @Schema(description = "UUID do usuário")
        UUID id,

        @Schema(description = "Email")
        String email,

        @Schema(description = "Hash BCrypt da senha")
        String passwordHash,

        @Schema(description = "Role")
        UserRole role,

        @Schema(description = "Status ativo")
        Boolean active
    ) {}
}
