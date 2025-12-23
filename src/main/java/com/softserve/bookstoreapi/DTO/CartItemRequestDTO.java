package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequestDTO(
        @NotNull Long bookId,
        @Min(1) int quantity
) {}