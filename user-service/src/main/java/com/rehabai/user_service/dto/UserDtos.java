package com.rehabai.user_service.dto;

import com.rehabai.user_service.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class UserDtos {

    public record CreateRequest(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Nome completo é obrigatório")
        String fullName,

        @NotBlank(message = "passwordHash é obrigatório (hash de senha)")
        String passwordHash,

        UserRole role
    ) {}

    public record UpdateRequest(
        String fullName,
        UserRole role,
        Boolean active
    ) {}

    public record ChangePasswordRequest(
        @NotBlank String passwordHash
    ) {}

    public record ChangeRoleRequest(
        @NotNull UserRole role
    ) {}

    public record Response(
        UUID id,
        String email,
        String fullName,
        UserRole role,
        Boolean active
    ) {}

    // Exposto apenas para consumo interno entre serviços (não via gateway)
    public record CredentialsResponse(
        UUID id,
        String email,
        String passwordHash,
        UserRole role,
        Boolean active
    ) {}
}
