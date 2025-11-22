package com.rehabai.prescription_service.events;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PatientDataResponseEvent(
        UUID requestId,
        UUID userId,
        String traceId,
        PatientMedicalData medicalData
) {
    public record PatientMedicalData(
            List<ConditionInfo> conditions,
            List<AllergyInfo> allergies,
            List<MedicationInfo> medications
    ) {}

    public record ConditionInfo(
            String code,
            String description,
            LocalDate onsetDate,
            LocalDate resolvedDate
    ) {}

    public record AllergyInfo(
            String substance,
            String reaction,
            String severity
    ) {}

    public record MedicationInfo(
            String drugName,
            String dose,
            String route,
            String frequency,
            LocalDate startDate,
            LocalDate endDate
    ) {}
}

