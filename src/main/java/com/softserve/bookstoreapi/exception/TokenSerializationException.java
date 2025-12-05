package com.softserve.bookstoreapi.exception;

public class TokenSerializationException extends RuntimeException {

    public TokenSerializationException(String message) {
        super(message);
    }

    public TokenSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

