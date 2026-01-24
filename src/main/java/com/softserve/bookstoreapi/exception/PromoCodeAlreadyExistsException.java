package com.softserve.bookstoreapi.exception;

public class PromoCodeAlreadyExistsException extends RuntimeException {
    public PromoCodeAlreadyExistsException(String message) {
        super(message);
    }
}
