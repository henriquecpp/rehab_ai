package com.rehabai.prescription_service.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "extractions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Extraction {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Lob
    @Column(name = "findings_json")
    private String findingsJson;

    @Lob
    @Column(name = "contraindications_json")
    private String contraindicationsJson;

    @Column(name = "model_used", length = 100)
    private String modelUsed;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}