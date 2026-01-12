package com.softserve.bookstoreapi.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

import java.util.UUID;

@Getter
public class TokenDeactivatedException extends AuthenticationException {
    private final UUID tokenId;

    public TokenDeactivatedException(String message, UUID tokenId) {
        super(message);
        this.tokenId = tokenId;
    }
}

