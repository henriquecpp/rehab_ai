package com.rehabai.plan_service.repository;

import com.rehabai.plan_service.model.PlanAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanAuditLogRepository extends JpaRepository<PlanAuditLog, UUID> {

    List<PlanAuditLog> findByPlanIdOrderByTimestampDesc(UUID planId);
}

