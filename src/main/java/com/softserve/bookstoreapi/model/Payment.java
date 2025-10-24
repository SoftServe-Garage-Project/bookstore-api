package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.model.enums.PaymentStatus;
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
@Table(name = "payments")
public class Payment extends AuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "payer_email", nullable = false)
    private User payer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_email", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod method;

    private String cardNumber;

    private String payPalEmail;
}