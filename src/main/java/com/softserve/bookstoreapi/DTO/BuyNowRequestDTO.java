package com.softserve.bookstoreapi.dto;

import com.softserve.bookstoreapi.model.PromoCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BuyNowRequestDTO(
        @NotNull(message = "Book ID is required")
        Long bookId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        String promoCode,

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Shipping address is required")
        String shippingAddress
) {}