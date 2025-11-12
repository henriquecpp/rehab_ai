package com.rehabai.prescription_service.repository;

import com.rehabai.prescription_service.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    Optional<Prescription> findTopByNormalizationIdOrderByCreatedAtDesc(UUID normalizationId);

    /**
     * Busca todas as prescrições de um arquivo específico
     */
    @Query("SELECT p FROM Prescription p " +
           "JOIN Normalization n ON p.normalizationId = n.id " +
           "JOIN Extraction e ON n.extractionId = e.id " +
           "WHERE e.fileId = :fileId " +
           "ORDER BY p.createdAt DESC")
    List<Prescription> findAllByFileId(@Param("fileId") UUID fileId);

    /**
     * Busca todas as prescrições de workflows de um usuário
     */
    @Query("SELECT p FROM Prescription p " +
           "JOIN Normalization n ON p.normalizationId = n.id " +
           "JOIN Extraction e ON n.extractionId = e.id " +
           "JOIN WorkflowRun w ON e.fileId = w.fileId " +
           "WHERE w.userId = :userId " +
           "ORDER BY p.createdAt DESC")
    List<Prescription> findAllByUserId(@Param("userId") UUID userId);
}
