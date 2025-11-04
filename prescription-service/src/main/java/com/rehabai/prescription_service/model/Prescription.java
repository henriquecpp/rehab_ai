package com.rehabai.prescription_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescriptions", indexes = {
        @Index(name = "idx_prescriptions_normalization", columnList = "normalization_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
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
}
