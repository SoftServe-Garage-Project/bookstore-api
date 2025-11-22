package com.softserve.bookstoreapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "deactivated_token")
@NoArgsConstructor
@AllArgsConstructor
public class DeactivatedToken {
    @Id
    private UUID id;

    @Column(name = "deactivated_at", nullable = false, updatable = false)
    private Instant deactivatedAt;

    @Column(name = "keep_until", nullable = false)
    private Instant keepUntil;

    @PrePersist
    public void prePersist() {
        if (deactivatedAt == null) {
            deactivatedAt = Instant.now();
        }
    }
}