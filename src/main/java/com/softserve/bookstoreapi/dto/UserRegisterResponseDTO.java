package com.softserve.bookstoreapi.dto;

import com.softserve.bookstoreapi.model.enums.UserRole;

import java.math.BigDecimal;

public record UserRegisterResponseDTO(
        Long id,
        String username,
        String email,
        UserRole role,
        BigDecimal balance
) {
}
