package com.softserve.bookstoreapi.exception;

public class PasswordRecoveryTokenExpiredException extends RuntimeException {
    public PasswordRecoveryTokenExpiredException(String message) {
        super(message);
    }
}
