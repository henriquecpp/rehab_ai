package com.rehabai.prescription_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_traces", indexes = {
        @Index(name = "idx_ai_traces_trace", columnList = "trace_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class AiTrace {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "trace_id", nullable = false, length = 100)
    private String traceId;

    @Column(name = "agent_name", length = 100)
    private String agentName;

    @Lob
    @Column(name = "input_summary")
    private String inputSummary;

    @Lob
    @Column(name = "output_summary")
    private String outputSummary;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "blocked_by_guardrail")
    private Boolean blockedByGuardrail = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
