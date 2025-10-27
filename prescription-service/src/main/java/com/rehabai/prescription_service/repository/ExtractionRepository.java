package com.rehabai.prescription_service.repository;

import com.rehabai.prescription_service.model.Extraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExtractionRepository extends JpaRepository<Extraction, UUID> {
    Optional<Extraction> findTopByFileIdOrderByCreatedAtDesc(UUID fileId);
}
