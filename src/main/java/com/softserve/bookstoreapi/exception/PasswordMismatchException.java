package com.softserve.bookstoreapi.exception;

import lombok.Getter;

@Getter
public class PasswordMismatchException extends RuntimeException {

    private final String errorCode;
    private final String email;

    public PasswordMismatchException(String errorCode, String email) {
        super(errorCode);
        this.errorCode = errorCode;
        this.email = email;
    }
}

