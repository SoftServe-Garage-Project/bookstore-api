package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TopUpDTO(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be > 0")
        BigDecimal amount,
        String senderDetails,
        String recipientDetails
) {}
