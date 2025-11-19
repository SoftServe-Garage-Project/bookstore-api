package com.softserve.bookstoreapi.dto;

import com.softserve.bookstoreapi.model.enums.Permissions;
import com.softserve.bookstoreapi.model.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AccountDTO(
        Long id,
        String username,
        String email,
        UserRole role,
        BigDecimal balance,
        List<Permissions> permissions,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
