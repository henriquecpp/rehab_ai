package com.rehabai.prescription_service.events;

import java.util.UUID;


public record PatientDataRequestEvent(
        UUID requestId,
        UUID userId,
        String traceId
) {}

