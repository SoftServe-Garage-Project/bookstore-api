package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.enums.OrderStatus;
import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.model.generaEntities.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "order",
        indexes = {
                @Index(name = "idx_order_user_created_desc", columnList = "user_id, created_at DESC"),
                @Index(name = "idx_order_status_active", columnList = "status, active"),
                @Index(name = "idx_order_promo_active", columnList = "promo_code_id, active"),
                @Index(name = "idx_order_total_amount_status", columnList = "total_amount, status")
        }
)
public class Order extends SoftDeletableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;
}
