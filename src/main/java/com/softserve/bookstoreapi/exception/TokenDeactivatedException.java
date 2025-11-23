package com.softserve.bookstoreapi.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class TokenDeactivatedException extends RuntimeException {
    private final UUID tokenId;

    public TokenDeactivatedException(String message, UUID tokenId) {
        super(message);
        this.tokenId = tokenId;
    }
}

