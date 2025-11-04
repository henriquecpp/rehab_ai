package com.rehabai.prescription_service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StartRequest(@NotNull UUID userId, @NotNull UUID fileId, String traceId) {}

