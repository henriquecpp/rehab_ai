package com.rehabai.prescription_service.dto;

import com.rehabai.prescription_service.model.PrescriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Schema(description = "DTO para a resposta de uma prescrição processada.")
public class PrescriptionResponse {

    @Schema(description = "ID da prescrição.", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID id;

    @Schema(description = "ID do arquivo original que gerou a prescrição.", example = "b2c3d4e5-f6a7-8901-2345-67890abcdef1")
    private UUID fileId;

    @Schema(description = "ID do usuário (clinician/admin) que fez o upload do arquivo.", example = "c3d4e5f6-a7b8-9012-3456-7890abcdef12")
    private UUID userId;

    @Schema(description = "ID da etapa de normalização associada.", example = "d4e5f6a7-b8c9-0123-4567-890abcdef123")
    private UUID normalizationId;

    @Schema(description = "Texto original extraído do documento (OCR).")
    private String originalText;

    @Schema(description = "Texto limpo e pré-processado antes da normalização.")
    private String cleanText;

    @Schema(description = "Dados estruturados da prescrição em formato JSON.", example = "{\"exercise\":\"Leg Press\",\"sets\":3,\"reps\":12}")
    private String jsonData;

    @Schema(description = "Status atual do processamento da prescrição.")
    private PrescriptionStatus status;

    @Schema(description = "Motivo da falha, caso o status seja FAILED.")
    private String failureReason;

    @Schema(description = "Data e hora de criação da prescrição.")
    private OffsetDateTime createdAt;

    @Schema(description = "Data e hora da última atualização.")
    private OffsetDateTime updatedAt;

    @Schema(description = "Metadados do arquivo que gerou esta prescrição.")
    private FileMetadata fileMetadata;

    @Data
    @Schema(description = "Informações do arquivo original que gerou a prescrição.")
    public static class FileMetadata {
        @Schema(description = "ID do arquivo", example = "f1e2d3c4-b5a6-7890-1234-567890abcdef")
        private UUID fileId;

        @Schema(description = "Nome original do arquivo", example = "laudo_joelho_paciente.pdf")
        private String fileName;

        @Schema(description = "Tipo do arquivo", example = "MEDICAL_REPORT")
        private String fileType;

        @Schema(description = "URL para visualização inline do arquivo", example = "/files/f1e2d3c4-b5a6-7890-1234-567890abcdef/view")
        private String viewUrl;

        @Schema(description = "URL para download do arquivo", example = "/files/f1e2d3c4-b5a6-7890-1234-567890abcdef/download")
        private String downloadUrl;
    }
}

