package com.rehabai.plan_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "plan_audit_logs", indexes = {
    @Index(name = "idx_audit_plan", columnList = "plan_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class PlanAuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "change_diff", columnDefinition = "jsonb")
    private String changeDiff;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp = OffsetDateTime.now();
}

