package com.rehabai.file_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "anonymization_log", indexes = {
        @Index(name = "idx_anonymization_file", columnList = "file_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class AnonymizationLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "rule_applied", length = 255)
    private String ruleApplied;

    @Column(name = "field_changed", length = 255)
    private String fieldChanged;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp = OffsetDateTime.now();
}
