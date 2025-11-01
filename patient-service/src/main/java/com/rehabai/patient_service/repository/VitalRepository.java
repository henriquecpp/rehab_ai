package com.rehabai.patient_service.repository;

import com.rehabai.patient_service.model.Vital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VitalRepository extends JpaRepository<Vital, UUID> {
    List<Vital> findByUserIdOrderByRecordedAtDesc(UUID userId);
}

