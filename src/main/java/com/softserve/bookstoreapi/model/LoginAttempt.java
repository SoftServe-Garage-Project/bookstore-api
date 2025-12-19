package com.softserve.bookstoreapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for tracking failed login attempts and implementing rate limiting.
 * Used to prevent brute-force attacks by blocking IPs after too many failed attempts.
 */
@Entity
@Table(name = "login_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * IP address or other identifier for the login attempt source
     */
    @Column(nullable = false, unique = true)
    private String identifier;

    /**
     * Number of failed login attempts
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    /**
     * Timestamp of the last failed attempt
     */
    @Column(nullable = false)
    private LocalDateTime lastAttemptTime;

    /**
     * If set, the identifier is blocked until this time
     */
    @Column
    private LocalDateTime blockedUntil;
}
