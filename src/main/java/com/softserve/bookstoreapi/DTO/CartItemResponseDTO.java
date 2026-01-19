package com.softserve.bookstoreapi.dto;

import java.math.BigDecimal;

public record CartItemResponseDTO(
        Long id,
        Long bookId,
        String bookTitle,
        int quantity,
        BigDecimal price
) {}