package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.generaEntities.SoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "promo_codes",
        indexes = {@Index(name = "idx_promo_codes_code_active", columnList = "code, is_active")}
)
public class PromoCode extends SoftDeletableEntity {

    @NotBlank(message = "{validation.promocode.code.notblank}")
    @Size(min = 3, max = 20, message = "{validation.promocode.code.size}")
    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @NotNull(message = "{validation.promocode.discount.notnull}")
    @DecimalMin(value = "0.01", inclusive = false, message = "{validation.promocode.discount.min}")
    @DecimalMax(value = "99.99", inclusive = true, message = "{validation.promocode.discount.max}")
    @Column(nullable = false, precision = 5, scale = 2, name = "discount_percentage")
    private BigDecimal discountPercentage;

    @Size(max = 255, message = "{validation.promocode.description.size}")
    @Column(name = "description", length = 255)
    private String description;

    @NotNull(message = "{validation.promocode.validfrom.notnull}")
    @Column(nullable = false, name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Min(value = 1, message = "{validation.promocode.maxuses.min}")
    @Column(name = "max_uses")
    private Integer maxUses;

    @Min(value = 0, message = "{validation.promocode.currentuses.min}")
    @Column(name = "current_uses", nullable = false)
    private Integer currentUses = 0;

    @DecimalMin(value = "0.0", message = "{validation.promocode.minorder.min}")
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;
}
