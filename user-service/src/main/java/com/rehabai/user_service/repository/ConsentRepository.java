package com.rehabai.user_service.repository;

import com.rehabai.user_service.model.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRepository extends JpaRepository<Consent, UUID> {
    List<Consent> findByUserIdOrderByTimestampDesc(UUID userId);
    List<Consent> findByUserIdAndTypeOrderByTimestampDesc(UUID userId, String type);
    Optional<Consent> findTopByUserIdAndTypeOrderByTimestampDesc(UUID userId, String type);
}

