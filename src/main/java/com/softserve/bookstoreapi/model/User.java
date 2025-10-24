package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.model.generaEntities.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_users_email", columnList = "email")
        }
)
public class User extends Payment {

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Username cannot be null or empty")
    private String username;

    @Column(nullable = false, length = 150)
    @NotBlank(message = "Email cannot be null or empty")
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password cannot be null or empty")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role = UserRole.ROLE_CUSTOMER;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
}
