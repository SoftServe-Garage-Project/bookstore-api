package com.softserve.bookstoreapi.dto;

import java.time.LocalDateTime;

public record ReviewResponseDTO(
        Long id,
        Long bookId,
        String userName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}