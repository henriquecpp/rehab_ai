package com.rehabai.auth_service.repository;

import com.rehabai.auth_service.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);
    Optional<RefreshToken> findByTokenIdAndRevokedFalse(UUID tokenId);
    long deleteByUserIdAndRevokedTrue(UUID userId);
    long deleteByExpiresAtBefore(Instant instant);
}

