package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.*;
import lombok.Data; // Добавь это
import lombok.NoArgsConstructor; // И это
import lombok.AllArgsConstructor; // И это
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeDTO {

        private Long id;

        @NotBlank(message = "{validation.promocode.code.notblank}")
        @Size(min = 3, max = 20, message = "{validation.promocode.code.size}")
        private String code;

        @NotNull(message = "{validation.promocode.discount.notnull}")
        @DecimalMin(value = "0.01", inclusive = false, message = "{validation.promocode.discount.min}")
        @DecimalMax(value = "99.99", inclusive = true, message = "{validation.promocode.discount.max}")
        private BigDecimal discountPercentage;

        @Size(max = 255, message = "{validation.promocode.description.size}")
        private String description;

        @NotNull(message = "{validation.promocode.validfrom.notnull}")
        private LocalDateTime validFrom;

        private LocalDateTime validTo;

        @Min(value = 1, message = "{validation.promocode.maxuses.min}")
        private Integer maxUses;

        private Integer currentUses;

        @DecimalMin(value = "0.0", message = "{validation.promocode.minorder.min}")
        private BigDecimal minOrderAmount;

        private Boolean isActive;
}