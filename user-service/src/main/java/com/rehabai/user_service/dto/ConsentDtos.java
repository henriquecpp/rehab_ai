package com.rehabai.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ConsentDtos {
    public record CreateRequest(
            @NotBlank String type,
            @NotNull Boolean granted,
            OffsetDateTime timestamp
    ) {}

    public record RevokeRequest(
            @NotBlank String type
    ) {}

    public record Response(
            UUID id,
            UUID userId,
            String type,
            Boolean granted,
            OffsetDateTime timestamp
    ) {}
}
