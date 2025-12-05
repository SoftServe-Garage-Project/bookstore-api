package com.softserve.bookstoreapi.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class RefreshTokenExpiredException extends RuntimeException {
    private final UUID tokenId;

    public RefreshTokenExpiredException(String message, UUID tokenId) {
        super(message);
        this.tokenId = tokenId;
    }
}

