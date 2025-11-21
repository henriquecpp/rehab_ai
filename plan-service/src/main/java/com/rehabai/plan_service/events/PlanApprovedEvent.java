package com.rehabai.plan_service.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlanApprovedEvent(
        UUID planId,
        UUID userId,
        String userEmail,
        UUID prescriptionId,
        UUID therapistId,
        UUID approvedBy,
        OffsetDateTime approvedAt,
        String planTitle,
        String planDescription
) {}

