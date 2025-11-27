package com.softserve.bookstoreapi.dto;

import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.model.enums.TransactionStatus;
import com.softserve.bookstoreapi.model.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionDTO(
        Long id,
        Long senderAccountId,
        Long receiverAccountId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status
) {}