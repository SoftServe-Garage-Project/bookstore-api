package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.exception.InvalidPromoCodeException;
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
        name = "promo_code",
        uniqueConstraints = @UniqueConstraint(name = "uq_promo_code", columnNames = "code"),
        indexes = {
                @Index(name = "idx_promo_code_active", columnList = "code, active"),
                @Index(name = "idx_promo_valid_dates_active", columnList = "valid_from, valid_to, active"),
                @Index(name = "idx_promo_min_amount_discount", columnList = "min_order_amount, discount_percentage"),
                @Index(name = "idx_promo_current_uses_active", columnList = "current_uses, active")
        }
)
public class PromoCode extends SoftDeletableEntity {

    @NotBlank(message = "Код промокоду обов'язковий")
    @Size(min = 3, max = 20, message = "Код повинен містити від 3 до 20 символів")
    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @NotNull(message = "Відсоток знижки обов'язковий")
    @DecimalMin(value = "0.0", inclusive = false, message = "Знижка повинна бути більше 0%")
    @DecimalMax(value = "100.0", inclusive = false, message = "Знижка не може бути 100% або більше")
    @Column(nullable = false, precision = 5, scale = 2, name = "discount_percentage")
    private BigDecimal discountPercentage;

    @Column(name = "description", length = 255)
    private String description;

    @NotNull(message = "Дата початку дії обов'язкова")
    @Column(nullable = false, name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;  // Кінець акції (null – безстроковий)

    @Min(value = 0, message = "Максимальна кількість використань не може бути від'ємною")
    @Column(name = "max_uses")
    private Integer maxUses;  // null – без обмежень, 100 – максимум 100 разів

    @Min(value = 0, message = "Поточна кількість використань не може бути від'ємною")
    @Column(name = "current_uses", nullable = false)
    private Integer currentUses = 0;

    @DecimalMin(value = "0.0", message = "Мінімальна сума не може бути від'ємною")
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;  // 0 – без обмежень

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        boolean timeValid = (validFrom == null || now.isAfter(validFrom)) &&
                (validTo == null || now.isBefore(validTo));
        boolean usesValid = maxUses == null || currentUses < maxUses;
        return super.isDeleted() ? false : timeValid && usesValid;
    }
}
