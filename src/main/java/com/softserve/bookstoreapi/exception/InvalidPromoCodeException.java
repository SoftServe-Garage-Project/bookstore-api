package com.softserve.bookstoreapi.exception;

public class InvalidPromoCodeException extends RuntimeException {
  public InvalidPromoCodeException(String message) {
    super(message);
  }
}
