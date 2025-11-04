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

    @Lob
    @Column(name = "normalized_terms")
    private String normalizedTerms;

    @Lob
    @Column(name = "rules_applied")
    private String rulesApplied;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}