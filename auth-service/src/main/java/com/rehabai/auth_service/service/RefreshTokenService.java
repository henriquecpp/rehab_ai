package com.rehabai.auth_service.service;

import com.rehabai.auth_service.model.RefreshToken;
import com.rehabai.auth_service.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${auth.refresh.expiration-ms}") long refreshExpirationMs) {
        this.repository = repository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    @Transactional
    public RefreshToken issueForUser(UUID userId) {
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        rt.setRevoked(false);
        return repository.save(rt);
    }

    @Transactional
    public RefreshToken rotate(UUID tokenId) {
        RefreshToken existing = repository.findByTokenIdAndRevokedFalse(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("invalid_refresh_token"));
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            existing.setRevoked(true);
            repository.save(existing);
            throw new IllegalArgumentException("expired_refresh_token");
        }
        existing.setRevoked(true);
        repository.save(existing);
        return issueForUser(existing.getUserId());
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        List<RefreshToken> active = repository.findByUserIdAndRevokedFalse(userId);
        for (RefreshToken t : active) {
            t.setRevoked(true);
        }
        repository.saveAll(active);
    }

    @Transactional
    public void revokeToken(UUID tokenId) {
        repository.findById(tokenId).ifPresent(t -> {
            t.setRevoked(true);
            repository.save(t);
        });
    }

    @Transactional
    public long cleanupExpired() {
        return repository.deleteByExpiresAtBefore(Instant.now());
    }
}

