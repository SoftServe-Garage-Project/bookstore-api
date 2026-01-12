package com.softserve.bookstoreapi.exception;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Exception thrown when a user is blocked due to too many failed login attempts.
 */
@Getter
public class TooManyLoginAttemptsException extends RuntimeException {

    private final String identifier;
    private final LocalDateTime blockedUntil;
    private final int remainingAttempts;

    public TooManyLoginAttemptsException(String message, String identifier, LocalDateTime blockedUntil) {
        super(message);
        this.identifier = identifier;
        this.blockedUntil = blockedUntil;
        this.remainingAttempts = 0;
    }

    public TooManyLoginAttemptsException(String message, String identifier, int remainingAttempts) {
        super(message);
        this.identifier = identifier;
        this.blockedUntil = null;
        this.remainingAttempts = remainingAttempts;
    }
}

