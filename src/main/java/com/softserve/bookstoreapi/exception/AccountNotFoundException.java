package com.softserve.bookstoreapi.exception;

public class AccountNotFoundException extends RuntimeException {
  public AccountNotFoundException(String message) {
    super(message);
  }
}
