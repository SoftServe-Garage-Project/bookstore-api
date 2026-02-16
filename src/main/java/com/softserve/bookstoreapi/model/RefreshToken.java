package com.softserve.bookstoreapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_user_email", columnList = "userEmail")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Column(nullable = false, updatable = false)
    private String userEmail;

    @Id
    @Column(nullable = false, unique = true)
    private UUID tokenId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private Instant expiresAt;

    @Setter
    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    @Setter
    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;
}
