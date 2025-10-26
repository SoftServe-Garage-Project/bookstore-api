package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.model.generaEntities.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payment_details")
public class PaymentDetails extends AuditableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "card_number", length = 20)
    private String cardNumber; // Только последние 4 цифры

    @Column(name = "card_holder_name", length = 100)
    private String cardHolderName;

    @Column(name = "card_expiry", length = 7)
    private String cardExpiry;

    @Column(name = "paypal_email", length = 150)
    private String payPalEmail;

    @Column(columnDefinition = "TEXT")
    private String description; // Дополнительно: "Использована при пополнении 26.10.2025"
}
