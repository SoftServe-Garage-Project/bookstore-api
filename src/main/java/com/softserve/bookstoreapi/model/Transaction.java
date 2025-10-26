package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.model.enums.TransactionType;
import com.softserve.bookstoreapi.model.enums.TransactionStatus;
import com.softserve.bookstoreapi.model.generaEntities.AuditableEntity;
import jakarta.persistence.*;
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
@Table(
        name = "transaction",
        indexes = {
                @Index(name = "idx_transaction_sender_created_desc", columnList = "sender_id, created_at DESC"),
                @Index(name = "idx_transaction_receiver_created_desc", columnList = "receiver_id, created_at DESC"),
                @Index(name = "idx_transaction_type_status_created", columnList = "type, status, created_at DESC"),
                @Index(name = "idx_transaction_order_id", columnList = "order_id"),
                @Index(name = "idx_transaction_payment_method_status", columnList = "payment_method, status"),
                @Index(name = "idx_transaction_amount_type", columnList = "amount, type")
        }
)
public class Transaction extends AuditableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Получатель
    // Может быть NULL для пополнения баланса извне
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    // Связь с заказом (только для type = PURCHASE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Для сохраненных карт
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_details_id")
    private PaymentDetails paymentDetails;
}
