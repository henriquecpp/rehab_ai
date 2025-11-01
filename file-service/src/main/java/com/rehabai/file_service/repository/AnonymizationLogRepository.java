package com.rehabai.file_service.repository;

import com.rehabai.file_service.model.AnonymizationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnonymizationLogRepository extends JpaRepository<AnonymizationLog, UUID> {
    List<AnonymizationLog> findByFileIdOrderByTimestampDesc(UUID fileId);
}

