package com.softserve.bookstoreapi.exception;

import lombok.Getter;

/**
 * Exception thrown when a user has reached the maximum number of allowed active tokens.
 */
@Getter
public class MaxTokensExceededException extends RuntimeException {

    private final String userEmail;
    private final int currentCount;
    private final int maxAllowed;

    public MaxTokensExceededException(String message, String userEmail, int currentCount, int maxAllowed) {
        super(message);
        this.userEmail = userEmail;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
    }
}

