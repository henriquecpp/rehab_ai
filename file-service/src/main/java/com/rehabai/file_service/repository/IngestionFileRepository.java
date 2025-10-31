package com.rehabai.file_service.repository;

import com.rehabai.file_service.model.FileStatus;
import com.rehabai.file_service.model.IngestionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IngestionFileRepository extends JpaRepository<IngestionFile, UUID> {
    List<IngestionFile> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<IngestionFile> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, FileStatus status);
}
