package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.generaEntities.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_item")
public class OrderItem extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Min(value = 1, message = "Кількість повинна бути не менше 1")
    @Column(nullable = false)
    private Integer quantity;

    // Оригинальная цена (без скидок)
    @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    // Скидка от книги
    @Column(name = "book_discount_percentage", precision = 5, scale = 2)
    private BigDecimal bookDiscountPercentage = BigDecimal.ZERO;

    // Скидка от промокода
    @Column(name = "promo_discount_percentage", precision = 5, scale = 2)
    private BigDecimal promoDiscountPercentage = BigDecimal.ZERO;

    // Финальная цена за единицу (после всех скидок)
    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;
}
