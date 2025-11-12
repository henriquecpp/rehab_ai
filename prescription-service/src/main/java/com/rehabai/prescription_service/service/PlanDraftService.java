package com.rehabai.prescription_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rehabai.prescription_service.dto.PlanDraftResponse;
import com.rehabai.prescription_service.dto.PlanDraftResponse.ExerciseDto;
import com.rehabai.prescription_service.model.Prescription;
import com.rehabai.prescription_service.repository.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável por converter prescrições geradas pela IA
 * em drafts de planos de reabilitação estruturados.
 */
@Service
public class PlanDraftService {

    private static final Logger log = LoggerFactory.getLogger(PlanDraftService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final ObjectMapper objectMapper;

    public PlanDraftService(PrescriptionRepository prescriptionRepository, ObjectMapper objectMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Gera um draft de plano a partir de uma prescrição existente.
     *
     * @param prescriptionId ID da prescrição processada pela IA
     * @param userId ID do usuário/paciente para quem o plano será criado
     * @return Draft do plano estruturado
     */
    public PlanDraftResponse generateDraft(UUID prescriptionId, UUID userId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found: " + prescriptionId));

        log.info("Generating plan draft from prescription {} for user {}", prescriptionId, userId);

        try {
            JsonNode prescriptionJson = objectMapper.readTree(prescription.getPrescriptionText());

            String title = extractString(prescriptionJson, "title", "Plano de Reabilitação Gerado por IA");
            String description = extractString(prescriptionJson, "description", "Plano gerado automaticamente a partir de laudo médico");
            String diagnosis = extractString(prescriptionJson, "diagnosis", "");
            Integer duration = extractInt(prescriptionJson, "duration", 30);
            String frequency = extractString(prescriptionJson, "frequency", "DIARIO");

            List<ExerciseDto> exercises = extractExercises(prescriptionJson);

            List<String> goals = extractGoals(prescriptionJson);

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(duration != null ? duration : 30);

            Double confidenceScore = extractConfidence(prescriptionJson);

            return new PlanDraftResponse(
                prescriptionId,
                userId,
                title,
                description,
                diagnosis,
                exercises,
                goals,
                duration,
                frequency,
                startDate,
                endDate,
                confidenceScore,
                prescription.getModelUsed(),
                prescription.getGuardrailStatus().name()
            );
        } catch (Exception e) {
            log.error("Failed to parse prescription JSON for {}: {}", prescriptionId, e.getMessage(), e);

            return new PlanDraftResponse(
                prescriptionId,
                userId,
                "Plano de Reabilitação (Revisão Necessária)",
                "Este plano foi gerado automaticamente mas requer revisão manual devido a erros no processamento.",
                "",
                new ArrayList<>(),
                List.of("Revisar laudo médico", "Definir objetivos específicos"),
                30,
                "DIARIO",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                0.0,
                prescription.getModelUsed(),
                prescription.getGuardrailStatus().name()
            );
        }
    }

    private List<ExerciseDto> extractExercises(JsonNode json) {
        List<ExerciseDto> exercises = new ArrayList<>();

        JsonNode exercisesNode = json.get("exercises");
        if (exercisesNode != null && exercisesNode.isArray()) {
            for (JsonNode exerciseNode : exercisesNode) {
                exercises.add(new ExerciseDto(
                    extractString(exerciseNode, "name", "Exercício não especificado"),
                    extractString(exerciseNode, "description", ""),
                    extractInt(exerciseNode, "sets", 3),
                    extractInt(exerciseNode, "repetitions", 10),
                    extractInt(exerciseNode, "duration", null),
                    extractString(exerciseNode, "frequency", "DIARIO")
                ));
            }
        }

        if (exercises.isEmpty()) {
            exercises.add(new ExerciseDto(
                "Exercício a definir",
                "Exercício deve ser especificado pelo clinician",
                3,
                10,
                null,
                "DIARIO"
            ));
        }

        return exercises;
    }

    private List<String> extractGoals(JsonNode json) {
        List<String> goals = new ArrayList<>();

        JsonNode goalsNode = json.get("goals");
        if (goalsNode != null && goalsNode.isArray()) {
            for (JsonNode goalNode : goalsNode) {
                goals.add(goalNode.asText());
            }
        }

        JsonNode objectivesNode = json.get("objectives");
        if (objectivesNode != null && objectivesNode.isArray()) {
            for (JsonNode objNode : objectivesNode) {
                goals.add(objNode.asText());
            }
        }

        if (goals.isEmpty()) {
            goals.add("Melhorar mobilidade e funcionalidade");
            goals.add("Reduzir dor e inflamação");
        }

        return goals;
    }

    private String extractString(JsonNode json, String fieldName, String defaultValue) {
        JsonNode node = json.get(fieldName);
        return node != null && !node.isNull() ? node.asText() : defaultValue;
    }

    private Integer extractInt(JsonNode json, String fieldName, Integer defaultValue) {
        JsonNode node = json.get(fieldName);
        return node != null && node.isNumber() ? node.asInt() : defaultValue;
    }

    private Double extractConfidence(JsonNode json) {
        JsonNode node = json.get("confidence");
        if (node != null && node.isNumber()) {
            return node.asDouble();
        }

        node = json.get("confidence_score");
        if (node != null && node.isNumber()) {
            return node.asDouble();
        }

        return null;
    }
}

