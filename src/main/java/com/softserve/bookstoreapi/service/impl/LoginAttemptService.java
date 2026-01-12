package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.model.LoginAttempt;
import com.softserve.bookstoreapi.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing login attempts and implementing rate limiting.
 * Tracks failed login attempts and blocks identifiers (IPs) after exceeding the limit.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.block-duration-minutes:15}")
    private int blockDurationMinutes;

    @Value("${security.login.attempt-expiry-hours:24}")
    private int attemptExpiryHours;

    /**
     * Records a failed login attempt for the given identifier.
     * If max attempts are exceeded, the identifier is blocked.
     *
     * @param identifier the IP address or other identifier
     */
    @Transactional
    public void loginFailed(String identifier) {
        LoginAttempt attempt = loginAttemptRepository.findByIdentifier(identifier)
                .orElseGet(() -> LoginAttempt.builder()
                        .identifier(identifier)
                        .attempts(0)
                        .lastAttemptTime(LocalDateTime.now())
                        .build());

        attempt.setAttempts(attempt.getAttempts() + 1);
        attempt.setLastAttemptTime(LocalDateTime.now());

        if (attempt.getAttempts() >= maxAttempts) {
            attempt.setBlockedUntil(LocalDateTime.now().plusMinutes(blockDurationMinutes));
            log.warn("Identifier blocked due to too many failed attempts: {}", maskIdentifier(identifier));
        } else {
            log.debug("Failed login attempt {} of {} for identifier: {}",
                    attempt.getAttempts(), maxAttempts, maskIdentifier(identifier));
        }

        loginAttemptRepository.save(attempt);
    }

    /**
     * Clears login attempts for a successful login.
     *
     * @param identifier the IP address or other identifier
     */
    @Transactional
    public void loginSucceeded(String identifier) {
        loginAttemptRepository.findByIdentifier(identifier)
                .ifPresent(attempt -> {
                    loginAttemptRepository.delete(attempt);
                    log.debug("Cleared login attempts for identifier: {}", maskIdentifier(identifier));
                });
    }

    /**
     * Checks if the identifier is currently blocked.
     *
     * @param identifier the IP address or other identifier
     * @return true if blocked, false otherwise
     */
    @Transactional
    public boolean isBlocked(String identifier) {
        return loginAttemptRepository.findByIdentifier(identifier)
                .map(attempt -> {
                    if (attempt.getBlockedUntil() != null) {
                        if (LocalDateTime.now().isBefore(attempt.getBlockedUntil())) {
                            return true;
                        }
                        // Block expired, clean up
                        loginAttemptRepository.delete(attempt);
                        log.debug("Block expired and cleared for identifier: {}", maskIdentifier(identifier));
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Gets the number of remaining login attempts before blocking.
     *
     * @param identifier the IP address or other identifier
     * @return remaining attempts
     */
    public int getRemainingAttempts(String identifier) {
        return loginAttemptRepository.findByIdentifier(identifier)
                .map(attempt -> Math.max(0, maxAttempts - attempt.getAttempts()))
                .orElse(maxAttempts);
    }

    /**
     * Gets the time when the block expires for the given identifier.
     *
     * @param identifier the IP address or other identifier
     * @return LocalDateTime when block expires, or null if not blocked
     */
    public LocalDateTime getBlockExpirationTime(String identifier) {
        return loginAttemptRepository.findByIdentifier(identifier)
                .filter(attempt -> attempt.getBlockedUntil() != null &&
                        LocalDateTime.now().isBefore(attempt.getBlockedUntil()))
                .map(LoginAttempt::getBlockedUntil)
                .orElse(null);
    }

    /**
     * Cleans up expired login attempts.
     * Called by scheduler to prevent database bloat.
     *
     * @return number of deleted records
     */
    @Transactional
    public int cleanupExpiredAttempts() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(attemptExpiryHours);
        int deleted = loginAttemptRepository.deleteExpiredAttempts(threshold, LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired login attempt records", deleted);
        }
        return deleted;
    }

    /**
     * Masks the identifier for logging (privacy protection).
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) {
            return "***";
        }
        return identifier.substring(0, 3) + "***";
    }
}
