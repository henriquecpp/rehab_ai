package com.rehabai.patient_service.repository;

import com.rehabai.patient_service.model.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConditionRepository extends JpaRepository<Condition, UUID> {
    List<Condition> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

