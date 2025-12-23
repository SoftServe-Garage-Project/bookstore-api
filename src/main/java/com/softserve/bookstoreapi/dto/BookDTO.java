package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record BookDTO(
        @NotBlank @Size(min = 2, max = 255)
        String title,

        String description,

        @NotBlank
        String genre,

        @NotBlank
        String ageGroup,

        @NotNull
        Integer publishedYear,

        @NotBlank
        String languageCode,

        @NotNull
        List<AuthorDTO> authors,

        @NotNull @DecimalMin("0.0")
        BigDecimal price,

        @NotNull @Min(0)
        Integer stockQuantity,

        @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal discountPercentage,

        @Min(1)
        Integer pageCount,

        String coverImageUrl
) {}

