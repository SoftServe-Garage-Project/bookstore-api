package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.model.generaEntities.SoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "user",
        uniqueConstraints = {@UniqueConstraint(name = "uq_user_email", columnNames = "email")},
        indexes = {
                @Index(name = "idx_user_email_active", columnList = "email, active"),
                @Index(name = "idx_user_username_active", columnList = "username, active"),
                @Index(name = "idx_user_role_active", columnList = "role, active"),
                @Index(name = "idx_user_balance_active", columnList = "balance, active"),
                @Index(name = "idx_user_role_created_desc", columnList = "role, created_at DESC")
        }
)
public class User extends SoftDeletableEntity {

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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission", length = 50)
    private Set<String> permissions = new HashSet<>();

    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));
        permissions.forEach(perm -> authorities.add(new SimpleGrantedAuthority(perm)));
        return authorities;
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }
}
