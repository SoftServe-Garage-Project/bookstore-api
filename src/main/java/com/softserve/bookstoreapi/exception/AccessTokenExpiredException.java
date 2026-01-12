package com.softserve.bookstoreapi.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

import java.util.UUID;

@Getter
public class AccessTokenExpiredException extends AuthenticationException {
    private final UUID tokenId;

    public AccessTokenExpiredException(String message, UUID tokenId) {
        super(message);
        this.tokenId = tokenId;
    }
}

