package com.rehabai.patient_service.repository;

import com.rehabai.patient_service.model.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, UUID> {
    List<ClinicalNote> findByUserIdOrderByTimestampDesc(UUID userId);
}

