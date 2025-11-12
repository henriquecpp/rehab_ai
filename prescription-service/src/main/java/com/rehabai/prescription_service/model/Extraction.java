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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "extractions", indexes = {
        @Index(name = "idx_extractions_file", columnList = "file_id")
})
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "findings_json", columnDefinition = "JSONB")
    private String findingsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contraindications_json", columnDefinition = "JSONB")
    private String contraindicationsJson;

    @Column(name = "model_used", length = 100)
    private String modelUsed;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}