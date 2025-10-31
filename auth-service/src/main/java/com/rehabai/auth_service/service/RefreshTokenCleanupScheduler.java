package com.rehabai.auth_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupScheduler.class);

    private final RefreshTokenService service;

    public RefreshTokenCleanupScheduler(RefreshTokenService service) {
        this.service = service;
    }

    // Run every hour
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        long removed = service.cleanupExpired();
        if (removed > 0) {
            log.info("Refresh token cleanup removed {} expired tokens", removed);
        }
    }
}

