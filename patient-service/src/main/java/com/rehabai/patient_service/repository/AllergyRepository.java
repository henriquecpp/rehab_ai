package com.rehabai.patient_service.repository;

import com.rehabai.patient_service.model.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AllergyRepository extends JpaRepository<Allergy, UUID> {
    List<Allergy> findByUserIdOrderByRecordedAtDesc(UUID userId);
}

