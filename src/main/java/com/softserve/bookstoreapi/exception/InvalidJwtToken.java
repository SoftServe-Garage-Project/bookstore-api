package com.softserve.bookstoreapi.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidJwtToken extends AuthenticationException {
    public InvalidJwtToken(String message) {
        super(message);
    }
}
