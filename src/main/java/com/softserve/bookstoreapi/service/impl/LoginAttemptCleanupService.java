package com.softserve.bookstoreapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for cleaning up expired login attempts.
 * Runs periodically to prevent database bloat from old login attempt records.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoginAttemptCleanupService {

    private final LoginAttemptService loginAttemptService;

    /**
     * Cleans up expired login attempts every hour.
     * Removes records that are no longer needed for rate limiting.
     */
    @Scheduled(fixedRateString = "${security.login.cleanup-interval-ms:3600000}")
    public void cleanupExpiredAttempts() {
        log.debug("Starting scheduled cleanup of expired login attempts");
        int deleted = loginAttemptService.cleanupExpiredAttempts();
        log.debug("Cleanup completed, {} records removed", deleted);
    }
}
