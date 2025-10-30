package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.generaEntities.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @Min(value = 1, message = "{validation.orderitem.quantity.min}")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "{validation.orderitem.originalprice.notnull}")
    @DecimalMin(value = "0.0", message = "{validation.orderitem.originalprice.min}")
    @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @DecimalMin(value = "0.0", message = "{validation.orderitem.bookdiscount.min}")
    @DecimalMax(value = "100.0", message = "{validation.orderitem.bookdiscount.max}")
    @Column(name = "book_discount_percentage", precision = 5, scale = 2)
    private BigDecimal bookDiscountPercentage = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "{validation.orderitem.promodiscount.min}")
    @DecimalMax(value = "100.0", message = "{validation.orderitem.promodiscount.max}")
    @Column(name = "promo_discount_percentage", precision = 5, scale = 2)
    private BigDecimal promoDiscountPercentage = BigDecimal.ZERO;

    @NotNull(message = "{validation.orderitem.finalprice.notnull}")
    @DecimalMin(value = "0.0", message = "{validation.orderitem.finalprice.min}")
    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;
}
