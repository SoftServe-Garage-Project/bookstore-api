package com.softserve.bookstoreapi.model.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Permissions implements GrantedAuthority {
    // Просто пример, потом будут реальные права
    READ_BOOKS, CREATE_BOOKS, UPDATE_BOOKS, DELETE_BOOKS,
    READ_AUTHORS, CREATE_AUTHORS, UPDATE_AUTHORS, DELETE_AUTHORS;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
