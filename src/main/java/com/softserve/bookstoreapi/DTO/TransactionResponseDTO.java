package com.softserve.bookstoreapi.dto;

import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.model.enums.TransactionStatus;
import com.softserve.bookstoreapi.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDTO (
        Long id,
        Long senderAccountId,
        Long receiverAccountId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        PaymentMethod paymentMethod,
        String description,
        Long orderId,
        LocalDateTime createdAt
) {}
