package com.rehabai.prescription_service.repository;

import com.rehabai.prescription_service.model.WorkflowRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowRunRepository extends JpaRepository<WorkflowRun, UUID> {
    Optional<WorkflowRun> findTopByFileIdOrderByCreatedAtDesc(UUID fileId);
}
