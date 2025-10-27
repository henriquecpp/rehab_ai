package com.rehabai.prescription_service.repository;

import com.rehabai.prescription_service.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    Optional<Prescription> findTopByNormalizationIdOrderByCreatedAtDesc(UUID normalizationId);
}
