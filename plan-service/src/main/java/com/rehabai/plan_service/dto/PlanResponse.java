package com.rehabai.plan_service.dto;

import com.rehabai.plan_service.model.PlanOrigin;
import com.rehabai.plan_service.model.PlanPriority;
import com.rehabai.plan_service.model.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Resposta com dados do plano de reabilitação")
public record PlanResponse(
    @Schema(description = "UUID do plano", example = "880e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "UUID do paciente", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID userId,

    @Schema(description = "UUID da prescrição origem", example = "770e8400-e29b-41d4-a716-446655440000")
    UUID prescriptionId,

    @Schema(description = "UUID do terapeuta responsável", example = "990e8400-e29b-41d4-a716-446655440000")
    UUID therapistId,

    @Schema(description = "Origem do plano", example = "AI_GENERATED")
    PlanOrigin origin,

    @Schema(description = "Confiança da IA", example = "0.87")
    Double confidenceScore,

    @Schema(description = "Número da versão (incrementa a cada update)", example = "3")
    Integer version,

    @Schema(description = "Status atual do plano", example = "APPROVED")
    PlanStatus status,

    @Schema(
        description = "Dados do plano em JSON (JSONB)",
        example = """
            {
              "title": "Plano de Reabilitação - Joelho",
              "diagnosis": "Gonartrose",
              "goals": ["Reduzir dor", "Melhorar mobilidade"],
              "exercises": [...]
            }
            """
    )
    String planData,

    @Schema(description = "Prioridade clínica", example = "HIGH")
    PlanPriority priority,

    @Schema(description = "Nível inicial de dor (0-10)", example = "6")
    Integer painLevelStart,

    @Schema(description = "Nível de dor esperado no fim do plano (0-10)", example = "2")
    Integer painLevelExpectedEnd,

    @Schema(description = "Data prevista de início do plano", example = "2025-11-18T09:00:00Z")
    OffsetDateTime startDate,

    @Schema(description = "Data prevista de término do plano", example = "2025-12-30T09:00:00Z")
    OffsetDateTime endDate,

    @Schema(description = "Etiquetas clínicas e de workflow")
    List<String> tags,

    @Schema(description = "Flag que indica se o plano está ativo", example = "true")
    boolean active,

    @Schema(description = "Motivo da última atualização", example = "Refinado por novos exames")
    String updateReason,

    @Schema(description = "Data de publicação do plano", example = "2025-11-20T12:00:00Z")
    OffsetDateTime publishedAt,

    @Schema(description = "UUID do usuário que publicou o plano", example = "880e8400-e29b-41d4-a716-446655440000")
    UUID publishedBy,

    @Schema(description = "Data da revisão clínica", example = "2025-11-21T09:30:00Z")
    OffsetDateTime reviewedAt,

    @Schema(description = "UUID do usuário que revisou", example = "990e8400-e29b-41d4-a716-446655440000")
    UUID reviewedBy,

    @Schema(description = "Data de criação", example = "2025-11-09T10:00:00Z")
    OffsetDateTime createdAt,

    @Schema(description = "Data da última atualização", example = "2025-11-09T15:30:00Z")
    OffsetDateTime updatedAt
) {}
