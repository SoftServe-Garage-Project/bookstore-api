package com.softserve.bookstoreapi.dto;

import com.softserve.bookstoreapi.model.enums.OrderStatus;
import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        BigDecimal totalAmount,
        OrderStatus status,
        PaymentMethod paymentMethod,
        LocalDateTime createdAt,
        List<OrderItemDTO> items
) {}