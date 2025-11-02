package com.rehabai.plan_service.repository;

import com.rehabai.plan_service.model.Plan;
import com.rehabai.plan_service.model.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    List<Plan> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Plan> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, PlanStatus status);

    List<Plan> findByPrescriptionIdOrderByVersionDesc(UUID prescriptionId);

    Optional<Plan> findTopByPrescriptionIdOrderByVersionDesc(UUID prescriptionId);

    @Query("SELECT MAX(p.version) FROM Plan p WHERE p.userId = :userId AND p.prescriptionId = :prescriptionId")
    Integer findMaxVersionByUserAndPrescription(@Param("userId") UUID userId, @Param("prescriptionId") UUID prescriptionId);

    Optional<Plan> findByUserIdAndPrescriptionIdAndVersion(UUID userId, UUID prescriptionId, Integer version);
}

