package com.softserve.bookstoreapi.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class RefreshTokenInvalidException extends RuntimeException {
    private final UUID tokenId;
    private final String reason;

    public RefreshTokenInvalidException(String message, UUID tokenId, String reason) {
        super(message);
        this.tokenId = tokenId;
        this.reason = reason;
    }
}

