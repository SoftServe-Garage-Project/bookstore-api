package com.softserve.bookstoreapi.exception;

public class RefreshTokenStorageException extends RuntimeException {

    public RefreshTokenStorageException(String message) {
        super(message);
    }

    public RefreshTokenStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
