package com.softserve.bookstoreapi.dto;

import java.math.BigDecimal;

public record BalanceDTO(
        BigDecimal balance,
        String currency
) {}