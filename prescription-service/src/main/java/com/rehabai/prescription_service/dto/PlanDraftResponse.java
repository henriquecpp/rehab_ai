package com.rehabai.prescription_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO representando um draft de plano de reabilitação gerado pela IA,
 * pronto para ser enviado ao plan-service para criação formal do plano.
 */
@Schema(description = "Draft de plano de reabilitação gerado pela IA (Bedrock)")
public record PlanDraftResponse(
    @Schema(description = "UUID da prescrição que originou o plano", example = "770e8400-e29b-41d4-a716-446655440000")
    UUID prescriptionId,

    @Schema(description = "UUID do paciente", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID userId,

    @Schema(description = "Título do plano", example = "Plano de Reabilitação - Joelho Direito")
    String title,

    @Schema(description = "Descrição detalhada", example = "Plano focado em recuperação de mobilidade...")
    String description,

    @Schema(description = "Diagnóstico médico", example = "Gonartrose bilateral")
    String diagnosis,

    @Schema(description = "Lista de exercícios recomendados")
    List<ExerciseDto> exercises,

    @Schema(description = "Objetivos terapêuticos", example = "[\"Reduzir dor\", \"Melhorar mobilidade\"]")
    List<String> goals,

    @Schema(description = "Duração total em semanas", example = "8")
    Integer duration,

    @Schema(description = "Frequência recomendada", example = "3x por semana")
    String frequency,

    @Schema(description = "Data de início sugerida", example = "2025-11-10")
    LocalDate startDate,

    @Schema(description = "Data de término prevista", example = "2026-01-05")
    LocalDate endDate,

    @Schema(description = "Score de confiança da IA (0-1)", example = "0.92")
    Double confidenceScore,

    @Schema(description = "Modelo de IA utilizado", example = "anthropic.claude-3-5-haiku")
    String modelUsed,

    @Schema(description = "Status do guardrail de segurança", example = "PASSED")
    String guardrailStatus
) {
    @Schema(description = "Exercício individual do plano")
    public record ExerciseDto(
        @Schema(description = "Nome do exercício", example = "Alongamento de quadríceps")
        String name,

        @Schema(description = "Descrição e instruções", example = "Em pé, segurar o pé...")
        String description,

        @Schema(description = "Número de séries", example = "3")
        Integer sets,

        @Schema(description = "Repetições por série", example = "15")
        Integer repetitions,

        @Schema(description = "Duração em segundos", example = "30")
        Integer duration,

        @Schema(description = "Frequência semanal", example = "3x/semana")
        String frequency
    ) {}
}

