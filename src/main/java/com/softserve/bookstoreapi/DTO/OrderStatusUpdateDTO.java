package com.softserve.bookstoreapi.dto;

import com.softserve.bookstoreapi.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateDTO(
        @NotNull(message = "Status is required")
        OrderStatus status
) {}