package com.rehabai.patient_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PatientDtos {

    // Patients
    public record CreateRequest(
            @NotBlank @Size(max = 150) String fullName,
            @Email @Size(max = 150) String email,
            @Size(max = 10) String dateOfBirth,
            @Size(max = 20) String gender
    ) {}

    public record UpdateRequest(
            @NotBlank @Size(max = 150) String fullName,
            @Email @Size(max = 150) String email,
            @Size(max = 10) String dateOfBirth,
            @Size(max = 20) String gender
    ) {}

    public record Response(
            UUID id,
            String fullName,
            String email,
            String dateOfBirth,
            String gender
    ) {}

    // Patient profile
    public record ProfileRequest(
            String preferredLanguage,
            String biologicalSex,
            LocalDate dateOfBirth,
            String notes
    ) {}

    public record ProfileResponse(
            UUID id,
            UUID userId,
            String preferredLanguage,
            String biologicalSex,
            LocalDate dateOfBirth,
            String notes,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {}

    // Clinical notes
    public record NoteCreateRequest(
            @NotBlank String note,
            UUID authorId,
            OffsetDateTime timestamp
    ) {}

    public record NoteResponse(
            UUID id,
            UUID userId,
            String note,
            UUID authorId,
            OffsetDateTime timestamp
    ) {}

    // Conditions
    public record ConditionCreateRequest(
            String code,
            String description,
            LocalDate onsetDate,
            LocalDate resolvedDate
    ) {}

    public record ConditionResponse(
            UUID id,
            UUID userId,
            String code,
            String description,
            LocalDate onsetDate,
            LocalDate resolvedDate,
            OffsetDateTime createdAt
    ) {}

    // Allergies
    public record AllergyCreateRequest(
            String substance,
            String reaction,
            String severity,
            OffsetDateTime recordedAt
    ) {}

    public record AllergyResponse(
            UUID id,
            UUID userId,
            String substance,
            String reaction,
            String severity,
            OffsetDateTime recordedAt
    ) {}

    // Medications
    public record MedicationCreateRequest(
            String drugName,
            String dose,
            String route,
            String frequency,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public record MedicationResponse(
            UUID id,
            UUID userId,
            String drugName,
            String dose,
            String route,
            String frequency,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    // Vitals
    public record VitalCreateRequest(
            @NotBlank String type,
            @NotNull String valueJson,
            OffsetDateTime recordedAt
    ) {}

    public record VitalResponse(
            UUID id,
            UUID userId,
            String type,
            String valueJson,
            OffsetDateTime recordedAt
    ) {}
}
