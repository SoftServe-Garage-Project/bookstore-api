package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record TopUpDTO(
        @DecimalMin(value = "0.01", message = "Amount must be > 0")
        BigDecimal amount,
        String senderDetails,
        String recipientDetails
) {}
