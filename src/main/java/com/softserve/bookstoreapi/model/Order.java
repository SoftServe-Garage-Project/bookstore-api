package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.enums.OrderStatus;
import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.model.generaEntities.SoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_orders_account_created_desc", columnList = "account_id, created_at DESC"),
                @Index(name = "idx_orders_status_active", columnList = "status, is_active")})
public class Order extends SoftDeletableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @NotNull(message = "{validation.order.totalamount.notnull}")
    @DecimalMin(value = "0.0", message = "{validation.order.totalamount.min}")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Valid
    @Size(min = 1, message = "{validation.order.items.size}")
    private List<OrderItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    @NotNull
    private String fullName;

    @Column(nullable = false)
    @NotNull
    private String shippingAddress;
}
