package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PromoCodeValidationRequestDTO(
        @NotBlank(message = "Promo code is required")
        String code,

        @NotNull(message = "Order amount is required")
        @DecimalMin(value = "0.0", message = "Order amount must be at least 0.0")
        BigDecimal orderAmount
) {}
