package com.rehabai.auth_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Microservices pattern: Periodic cleanup for eventual consistency.
 * - Expired tokens: cleaned hourly
 * - Orphaned tokens (user deleted): cleaned daily
 */
@Component
public class RefreshTokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupScheduler.class);

    private final RefreshTokenService service;

    public RefreshTokenCleanupScheduler(RefreshTokenService service) {
        this.service = service;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        try {
            long removed = service.cleanupExpired();
            if (removed > 0) {
                log.info("Hourly cleanup: removed {} expired tokens", removed);
            }
        } catch (Exception e) {
            log.error("Expired token cleanup failed - will retry next hour", e);
            // Don't rethrow - scheduler must continue running
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOrphanedTokens() {
        try {
            log.info("Starting daily orphaned tokens cleanup");
            long removed = service.cleanupOrphaned();
            if (removed > 0) {
                log.warn("Daily cleanup: removed {} orphaned tokens (users no longer exist)", removed);
            } else {
                log.info("Daily cleanup: no orphaned tokens found");
            }
        } catch (Exception e) {
            log.error("Orphaned token cleanup failed - will retry tomorrow", e);
            // Don't rethrow - scheduler must continue running
        }
    }
}
