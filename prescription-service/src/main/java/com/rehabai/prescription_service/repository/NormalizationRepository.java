package com.rehabai.prescription_service.repository;

import com.rehabai.prescription_service.model.Normalization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NormalizationRepository extends JpaRepository<Normalization, UUID> {
    Optional<Normalization> findTopByExtractionIdOrderByCreatedAtDesc(UUID extractionId);
}
