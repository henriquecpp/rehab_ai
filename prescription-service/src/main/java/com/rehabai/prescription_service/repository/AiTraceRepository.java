package com.rehabai.prescription_service.repository;

import com.rehabai.prescription_service.model.AiTrace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiTraceRepository extends JpaRepository<AiTrace, UUID> {
    List<AiTrace> findByTraceIdOrderByCreatedAtDesc(String traceId);
}
