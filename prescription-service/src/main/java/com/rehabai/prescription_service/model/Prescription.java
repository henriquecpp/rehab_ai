package com.rehabai.prescription_service.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescriptions")
public class Prescription {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "normalization_id", nullable = false)
    private UUID normalizationId;

    @Lob
    @Column(name = "prescription_text")
    private String prescriptionText;

    @Lob
    @Column(name = "parameters_json")
    private String parametersJson;

    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    @Column(name = "model_used", length = 100)
    private String modelUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "guardrail_status", length = 20)
    private GuardrailStatus guardrailStatus = GuardrailStatus.OK;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getNormalizationId() { return normalizationId; }
    public void setNormalizationId(UUID normalizationId) { this.normalizationId = normalizationId; }
    public String getPrescriptionText() { return prescriptionText; }
    public void setPrescriptionText(String prescriptionText) { this.prescriptionText = prescriptionText; }
    public String getParametersJson() { return parametersJson; }
    public void setParametersJson(String parametersJson) { this.parametersJson = parametersJson; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
    public GuardrailStatus getGuardrailStatus() { return guardrailStatus; }
    public void setGuardrailStatus(GuardrailStatus guardrailStatus) { this.guardrailStatus = guardrailStatus; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

