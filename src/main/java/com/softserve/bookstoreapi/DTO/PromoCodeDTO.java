package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromoCodeDTO(
        Long id,

        @NotBlank(message = "{validation.promocode.code.notblank}")
        @Size(min = 3, max = 20, message = "{validation.promocode.code.size}")
        String code,

        @NotNull(message = "{validation.promocode.discount.notnull}")
        @DecimalMin(value = "0.01", inclusive = false, message = "{validation.promocode.discount.min}")
        @DecimalMax(value = "99.99", inclusive = true, message = "{validation.promocode.discount.max}")
        BigDecimal discountPercentage,

        @Size(max = 255, message = "{validation.promocode.description.size}")
        String description,

        @NotNull(message = "{validation.promocode.validfrom.notnull}")
        LocalDateTime validFrom,

        LocalDateTime validTo,

        @Min(value = 1, message = "{validation.promocode.maxuses.min}")
        Integer maxUses,

        Integer currentUses,

        @DecimalMin(value = "0.0", message = "{validation.promocode.minorder.min}")
        BigDecimal minOrderAmount,

        Boolean isActive
) {}
