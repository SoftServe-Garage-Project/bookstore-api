package com.softserve.bookstoreapi.dto;

import java.math.BigDecimal;

public record OrderItemDTO(
        Long bookId,
        String bookTitle,
        int quantity,
        BigDecimal originalPrice,
        BigDecimal finalPrice,
        BigDecimal bookDiscountPercentage
) {}