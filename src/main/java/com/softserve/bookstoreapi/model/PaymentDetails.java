package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.model.generaEntities.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
    @JoinColumn(name = "account_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Size(min = 4, max = 20, message = "{validation.paymentdetails.cardnumber.size}")
    @Column(name = "card_number", length = 20)
    private String cardNumber;

    @Size(max = 100, message = "{validation.paymentdetails.cardholder.size}")
    @Column(name = "card_holder_name", length = 100)
    private String cardHolderName;

    @Size(max = 7, message = "{validation.paymentdetails.expiry.size}")
    @Column(name = "card_expiry", length = 7)
    private String cardExpiry;

    @Size(max = 150, message = "{validation.paymentdetails.paypal.size}")
    @Column(name = "paypal_email", length = 150)
    private String payPalEmail;

    @Column(columnDefinition = "TEXT")
    private String description;
}
