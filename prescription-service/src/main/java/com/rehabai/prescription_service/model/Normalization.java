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
@Table(name = "normalizations", indexes = {
        @Index(name = "idx_normalizations_extraction", columnList = "extraction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Normalization {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "extraction_id", nullable = false)
    private UUID extractionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "normalized_terms", columnDefinition = "JSONB")
    private String normalizedTerms;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rules_applied", columnDefinition = "JSONB")
    private String rulesApplied;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}