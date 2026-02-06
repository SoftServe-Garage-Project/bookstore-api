package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.*;

public record ReviewRequestDTO(
        @NotNull(message = "Book ID is required")
        Long bookId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @NotBlank(message = "Comment cannot be empty")
        @Size(max = 1000, message = "Comment must not exceed 1000 characters")
        String comment
) {}