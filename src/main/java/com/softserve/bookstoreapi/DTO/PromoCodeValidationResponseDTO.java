package com.softserve.bookstoreapi.dto;

import java.math.BigDecimal;

public record PromoCodeValidationResponseDTO(
        boolean valid,
        String message,
        BigDecimal discountPercentage,
        BigDecimal discountAmount,
        BigDecimal finalAmount
) {}
