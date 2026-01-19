package com.softserve.bookstoreapi.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDTO(
        Long id,
        List<CartItemResponseDTO> items,
        BigDecimal totalPrice
) {}