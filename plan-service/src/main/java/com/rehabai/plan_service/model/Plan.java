package com.rehabai.plan_service.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "plans",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_plan_version",
                        columnNames = {"user_id", "prescription_id", "version"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Plan {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId; // Paciente

    @Column(name = "prescription_id", nullable = false)
    private UUID prescriptionId;

    @Column(name = "therapist_id", nullable = false)
    private UUID therapistId; // Profissional responsável

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", nullable = false, length = 30)
    private PlanOrigin origin = PlanOrigin.AI_GENERATED;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "plan_data", nullable = false, columnDefinition = "jsonb")
    private JsonNode planData;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlanStatus status = PlanStatus.DRAFT;

    @Column(name = "update_reason", length = 500)
    private String updateReason;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "published_by")
    private UUID publishedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private PlanPriority priority = PlanPriority.MEDIUM;

    @Column(name = "pain_level_start")
    private Integer painLevelStart; // 0–10

    @Column(name = "pain_level_expected_end")
    private Integer painLevelExpectedEnd; // 0–10

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @ElementCollection
    @CollectionTable(
            name = "plan_tags",
            joinColumns = @JoinColumn(name = "plan_id")
    )
    @Column(name = "tag")
    private List<String> tags;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (createdAt == null) {
            createdAt = now;
        }
        if (startDate == null) {
            startDate = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
