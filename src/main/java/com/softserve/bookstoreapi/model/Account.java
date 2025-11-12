package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.enums.Permissions;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.model.generaEntities.SoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {@UniqueConstraint(name = "uq_accounts_email", columnNames = "email")},
        indexes = {@Index(name = "idx_accounts_email_active", columnList = "email, is_active")}
)
public class Account extends SoftDeletableEntity {

    @NotBlank(message = "{validation.user.username.notblank}")
    @Size(min = 3, max = 100, message = "{validation.user.username.size}")
    @Column(nullable = false, length = 100)
    private String username;

    @NotBlank(message = "{validation.user.email.notblank}")
    @Email(message = "{validation.user.email.invalid}")
    @Size(max = 150, message = "{validation.user.email.size}")
    @Column(nullable = false, length = 150)
    private String email;

    @NotBlank(message = "{validation.user.password.notblank}")
    @Size(min = 8, max = 255, message = "{validation.user.password.size}")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role = UserRole.ROLE_CUSTOMER;

    @DecimalMin(value = "0.0", message = "{validation.user.balance.min}")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "account_permissions", joinColumns = @JoinColumn(name = "account_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", length = 50)
    private List<Permissions> permissions = new ArrayList<>();

    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));
        authorities.addAll(permissions);
        return authorities;
    }
}

