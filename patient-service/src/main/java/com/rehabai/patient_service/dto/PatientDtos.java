package com.rehabai.patient_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PatientDtos {

    // Patients
    @Schema(description = "Requisição para criar paciente")
    public record CreateRequest(
            @Schema(description = "Nome completo", example = "Maria Santos", required = true, maxLength = 150)
            @NotBlank @Size(max = 150) String fullName,

            @Schema(description = "Email", example = "maria@example.com", maxLength = 150)
            @Email @Size(max = 150) String email,

            @Schema(description = "Data de nascimento (YYYY-MM-DD)", example = "1990-05-15", maxLength = 10)
            @Size(max = 10) String dateOfBirth,

            @Schema(description = "Gênero", example = "Feminino", maxLength = 20)
            @Size(max = 20) String gender
    ) {}

    @Schema(description = "Requisição para atualizar paciente")
    public record UpdateRequest(
            @Schema(description = "Nome completo", example = "Maria Santos Silva", required = true)
            @NotBlank @Size(max = 150) String fullName,

            @Schema(description = "Email", example = "maria.santos@example.com")
            @Email @Size(max = 150) String email,

            @Schema(description = "Data de nascimento", example = "1990-05-15")
            @Size(max = 10) String dateOfBirth,

            @Schema(description = "Gênero", example = "Feminino")
            @Size(max = 20) String gender
    ) {}

    @Schema(description = "Resposta com dados do paciente")
    public record Response(
            @Schema(description = "UUID do paciente") UUID id,
            @Schema(description = "Nome completo") String fullName,
            @Schema(description = "Email") String email,
            @Schema(description = "Data de nascimento") String dateOfBirth,
            @Schema(description = "Gênero") String gender
    ) {}

    // Patient profile
    @Schema(description = "Requisição para perfil detalhado do paciente")
    public record ProfileRequest(
            @Schema(description = "Idioma preferido", example = "pt-BR")
            String preferredLanguage,

            @Schema(description = "Sexo biológico", example = "F", allowableValues = {"M", "F", "O"})
            String biologicalSex,

            @Schema(description = "Data de nascimento", example = "1990-05-15")
            LocalDate dateOfBirth,

            @Schema(description = "Notas adicionais", example = "Paciente com histórico de...")
            String notes
    ) {}

    @Schema(description = "Resposta com perfil detalhado do paciente")
    public record ProfileResponse(
            @Schema(description = "UUID do perfil") UUID id,
            @Schema(description = "UUID do usuário") UUID userId,
            @Schema(description = "Idioma preferido") String preferredLanguage,
            @Schema(description = "Sexo biológico") String biologicalSex,
            @Schema(description = "Data de nascimento") LocalDate dateOfBirth,
            @Schema(description = "Notas") String notes,
            @Schema(description = "Data de criação") OffsetDateTime createdAt,
            @Schema(description = "Data de atualização") OffsetDateTime updatedAt
    ) {}

    // Clinical notes
    @Schema(description = "Requisição para criar anotação clínica")
    public record NoteCreateRequest(
            @Schema(description = "Conteúdo da anotação", example = "Paciente apresenta melhora...", required = true)
            @NotBlank String note,

            @Schema(description = "UUID do autor (profissional)", example = "660e8400-e29b-41d4-a716-446655440000")
            UUID authorId,

            @Schema(description = "Timestamp da anotação", example = "2025-11-09T10:00:00Z")
            OffsetDateTime timestamp
    ) {}

    @Schema(description = "Resposta com anotação clínica")
    public record NoteResponse(
            @Schema(description = "UUID da anotação") UUID id,
            @Schema(description = "UUID do usuário/paciente") UUID userId,
            @Schema(description = "Conteúdo da anotação") String note,
            @Schema(description = "UUID do autor") UUID authorId,
            @Schema(description = "Timestamp") OffsetDateTime timestamp
    ) {}

    // Conditions
    @Schema(description = "Requisição para criar condição médica")
    public record ConditionCreateRequest(
            @Schema(description = "Código da condição (CID-10)", example = "M25.561")
            String code,

            @Schema(description = "Descrição", example = "Dor no joelho direito")
            String description,

            @Schema(description = "Data de início", example = "2025-01-15")
            LocalDate onsetDate,

            @Schema(description = "Data de resolução (se resolvida)", example = "2025-03-20")
            LocalDate resolvedDate
    ) {}

    @Schema(description = "Resposta com condição médica")
    public record ConditionResponse(
            @Schema(description = "UUID da condição") UUID id,
            @Schema(description = "UUID do usuário") UUID userId,
            @Schema(description = "Código") String code,
            @Schema(description = "Descrição") String description,
            @Schema(description = "Data de início") LocalDate onsetDate,
            @Schema(description = "Data de resolução") LocalDate resolvedDate,
            @Schema(description = "Data de criação") OffsetDateTime createdAt
    ) {}

    // Allergies
    @Schema(description = "Requisição para criar alergia")
    public record AllergyCreateRequest(
            @Schema(description = "Substância", example = "Penicilina")
            String substance,

            @Schema(description = "Reação", example = "Erupção cutânea")
            String reaction,

            @Schema(description = "Severidade", example = "Moderada", allowableValues = {"Leve", "Moderada", "Grave"})
            String severity,

            @Schema(description = "Data de registro", example = "2025-11-09T10:00:00Z")
            OffsetDateTime recordedAt
    ) {}

    @Schema(description = "Resposta com alergia")
    public record AllergyResponse(
            @Schema(description = "UUID da alergia") UUID id,
            @Schema(description = "UUID do usuário") UUID userId,
            @Schema(description = "Substância") String substance,
            @Schema(description = "Reação") String reaction,
            @Schema(description = "Severidade") String severity,
            @Schema(description = "Data de registro") OffsetDateTime recordedAt
    ) {}

    // Medications
    @Schema(description = "Requisição para criar medicação")
    public record MedicationCreateRequest(
            @Schema(description = "Nome do medicamento", example = "Ibuprofeno 400mg")
            String drugName,

            @Schema(description = "Dose", example = "400mg")
            String dose,

            @Schema(description = "Via de administração", example = "Oral")
            String route,

            @Schema(description = "Frequência", example = "8/8h")
            String frequency,

            @Schema(description = "Data de início", example = "2025-11-09")
            LocalDate startDate,

            @Schema(description = "Data de término", example = "2025-11-16")
            LocalDate endDate
    ) {}

    @Schema(description = "Resposta com medicação")
    public record MedicationResponse(
            @Schema(description = "UUID da medicação") UUID id,
            @Schema(description = "UUID do usuário") UUID userId,
            @Schema(description = "Nome do medicamento") String drugName,
            @Schema(description = "Dose") String dose,
            @Schema(description = "Via") String route,
            @Schema(description = "Frequência") String frequency,
            @Schema(description = "Data de início") LocalDate startDate,
            @Schema(description = "Data de término") LocalDate endDate
    ) {}

    // Vitals
    @Schema(description = "Requisição para criar sinal vital")
    public record VitalCreateRequest(
            @Schema(description = "Tipo de sinal vital", example = "blood_pressure", required = true)
            @NotBlank String type,

            @Schema(
                description = "Valor em formato JSON (estrutura flexível)",
                example = "{\"systolic\": 120, \"diastolic\": 80, \"unit\": \"mmHg\"}",
                required = true
            )
            @NotNull String valueJson,

            @Schema(description = "Data/hora do registro", example = "2025-11-09T10:00:00Z")
            OffsetDateTime recordedAt
    ) {}

    @Schema(description = "Resposta com sinal vital")
    public record VitalResponse(
            @Schema(description = "UUID do sinal vital") UUID id,
            @Schema(description = "UUID do usuário") UUID userId,
            @Schema(description = "Tipo") String type,
            @Schema(description = "Valor JSON") String valueJson,
            @Schema(description = "Data de registro") OffsetDateTime recordedAt
    ) {}
}

