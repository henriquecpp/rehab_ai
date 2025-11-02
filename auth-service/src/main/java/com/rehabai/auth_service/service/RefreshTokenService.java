package com.rehabai.auth_service.service;

import com.rehabai.auth_service.model.RefreshToken;
import com.rehabai.auth_service.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository repository;
    private final UserServiceClient userServiceClient;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repository,
                               UserServiceClient userServiceClient,
                               @Value("${auth.refresh.expiration-ms}") long refreshExpirationMs) {
        this.repository = repository;
        this.userServiceClient = userServiceClient;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    @Transactional
    public RefreshToken issueForUser(UUID userId) {
        try {
            userServiceClient.getById(userId);
        } catch (Exception e) {
            log.warn("Attempted to create refresh token for non-existent user: {}", userId);
            throw new IllegalArgumentException("user_not_found");
        }

        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        rt.setRevoked(false);
        return repository.save(rt);
    }

    @Transactional
    public RefreshToken rotate(UUID tokenId) {
        RefreshToken existing = repository.findByTokenIdAndRevokedFalseWithLock(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("invalid_refresh_token"));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            existing.setRevoked(true);
            repository.save(existing);
            throw new IllegalArgumentException("expired_refresh_token");
        }

        existing.setRevoked(true);
        repository.save(existing);
        repository.flush();
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
        log.debug("Starting cleanup of expired refresh tokens at {}", Instant.now());
        int deleted = repository.deleteByExpiresAtBefore(Instant.now());
        if (deleted > 0) {
            log.info("Cleanup removed {} expired refresh tokens", deleted);
        } else {
            log.debug("No expired tokens to clean up");
        }
        return deleted;
    }

    @Transactional
    public long cleanupOrphaned() {
        log.info("Starting cleanup of orphaned refresh tokens (paginated)");
        int deleted = 0;
        int page = 0;
        int pageSize = 50;
        boolean hasMore = true;

        while (hasMore) {
            List<RefreshToken> batch = repository.findAll(
                org.springframework.data.domain.PageRequest.of(page, pageSize)
            ).getContent();

            if (batch.isEmpty()) {
                hasMore = false;
                break;
            }

            for (RefreshToken token : batch) {
                try {
                    userServiceClient.getById(token.getUserId());
                } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                    repository.delete(token);
                    deleted++;
                    log.debug("Removed orphaned token {} for non-existent user {}",
                            token.getTokenId(), token.getUserId());
                } catch (Exception e) {
                    log.warn("Could not verify user {} for token {}: {}",
                            token.getUserId(), token.getTokenId(), e.getMessage());
                }
            }

            page++;
            if (page > 1000) {
                log.error("Orphan cleanup exceeded 1000 pages, stopping. Consider increasing page size.");
                break;
            }
        }

        if (deleted > 0) {
            log.info("Cleanup removed {} orphaned refresh tokens (processed {} pages)", deleted, page);
        } else {
            log.info("No orphaned tokens found (processed {} pages)", page);
        }
        return deleted;
    }
}
