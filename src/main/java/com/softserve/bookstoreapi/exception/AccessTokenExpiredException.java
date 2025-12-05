package com.softserve.bookstoreapi.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class AccessTokenExpiredException extends RuntimeException {
    private final UUID tokenId;

    public AccessTokenExpiredException(String message, UUID tokenId) {
        super(message);
        this.tokenId = tokenId;
    }
}

