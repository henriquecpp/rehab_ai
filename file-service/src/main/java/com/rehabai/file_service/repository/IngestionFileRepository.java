package com.rehabai.file_service.repository;

import com.rehabai.file_service.model.IngestionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IngestionFileRepository extends JpaRepository<IngestionFile, UUID> {
}

