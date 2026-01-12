package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Scheduled service for cleaning up expired and no longer needed tokens from the database.
 * This helps maintain database performance and removes stale authentication data.
 *
 * <p>Cleanup conditions:</p>
 * <ul>
 *   <li><b>Refresh Tokens:</b> Deleted when expired (expiresAt < now)</li>
 *   <li><b>Deactivated Tokens:</b> Deleted when keepUntil < now (original JWT would have expired anyway)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final DeactivatedTokenRepository deactivatedTokenRepository;

    /**
     * Scheduled task that runs every hour to clean up expired tokens.
     * Cron expression: "0 0 * * * *" means at minute 0 of every hour.
     *
     * <p>This method removes:</p>
     * <ul>
     *   <li>Expired refresh tokens (tokens past their expiration date)</li>
     *   <li>Deactivated access tokens that no longer need to be tracked</li>
     * </ul>
     */
    @Scheduled(cron = "${token.cleanup.cron:0 0 * * * *}")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled token cleanup task");

        Instant now = Instant.now();

        try {
            int deletedRefreshTokens = cleanupExpiredRefreshTokens(now);
            int deletedDeactivatedTokens = cleanupExpiredDeactivatedTokens(now);

            log.info("Token cleanup completed. Deleted {} expired refresh tokens and {} expired deactivated tokens",
                    deletedRefreshTokens, deletedDeactivatedTokens);
        } catch (Exception e) {
            log.error("Error during token cleanup task", e);
        }
    }

    /**
     * Cleans up expired refresh tokens from the database.
     * A refresh token is considered expired when its expiresAt timestamp is before the current time.
     *
     * @param now current timestamp
     * @return number of deleted refresh tokens
     */
    @Transactional
    public int cleanupExpiredRefreshTokens(Instant now) {
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(now);
        if (deletedCount > 0) {
            log.debug("Deleted {} expired refresh tokens", deletedCount);
        }
        return deletedCount;
    }

    /**
     * Cleans up expired deactivated tokens from the database.
     * A deactivated token can be removed when its keepUntil timestamp has passed,
     * meaning the original JWT would have expired anyway and doesn't need to be tracked anymore.
     *
     * @param now current timestamp
     * @return number of deleted deactivated tokens
     */
    @Transactional
    public int cleanupExpiredDeactivatedTokens(Instant now) {
        int deletedCount = deactivatedTokenRepository.deleteExpiredDeactivatedTokens(now);
        if (deletedCount > 0) {
            log.debug("Deleted {} expired deactivated tokens", deletedCount);
        }
        return deletedCount;
    }
}

