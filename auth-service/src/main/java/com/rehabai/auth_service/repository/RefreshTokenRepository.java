package com.rehabai.auth_service.repository;

import com.rehabai.auth_service.model.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);
    Optional<RefreshToken> findByTokenIdAndRevokedFalse(UUID tokenId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenId = :tokenId AND rt.revoked = false")
    Optional<RefreshToken> findByTokenIdAndRevokedFalseWithLock(@Param("tokenId") UUID tokenId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId AND rt.revoked = true")
    int deleteByUserIdAndRevokedTrue(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :instant")
    int deleteByExpiresAtBefore(@Param("instant") Instant instant);
}

