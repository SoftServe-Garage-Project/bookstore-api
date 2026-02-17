package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequestDTO(
        String promoCode,
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Shipping address is required")
        String shippingAddress
) {}