package com.softserve.bookstoreapi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class TokenFactory {
    private final Duration accessTokenDuration;
    private final Duration refreshTokenDuration;

    public Token createAccessToken(Authentication authentication) {
        if (authentication == null) {throw new IllegalArgumentException("Authentication cannot be null");}

        if (authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authentication name cannot be null or blank");
        }

        var authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            throw new IllegalArgumentException("Cannot create access token: no authorities assigned to user " + authentication.getName());
        }

        var authorityList = authorities.stream()
                .filter(Objects::nonNull)
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth != null && !auth.isBlank())
                .toList();

        if (authorityList.isEmpty()) {
            throw new IllegalArgumentException("Cannot create access token: all authorities are invalid for user " + authentication.getName());
        }

        var now = Instant.now();
        return new Token(UUID.randomUUID(), authentication.getName(), authorityList, now, now.plus(accessTokenDuration));
    }

    public Token createRefreshToken(Authentication authentication) {
        if (authentication == null) {throw new IllegalArgumentException("Authentication cannot be null");}

        if (authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authentication name cannot be null or blank");
        }

        var now = Instant.now();

        return new Token(
                UUID.randomUUID(),
                authentication.getName(),
                List.of("REFRESH_TOKEN"),
                now,
                now.plus(refreshTokenDuration)
        );
    }

    public Token createPasswordRecoveryToken(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        var now = Instant.now();

        return new Token(
                UUID.randomUUID(),
                email,
                List.of("PASSWORD_RECOVERY"),
                now,
                now.plus(Duration.ofMinutes(15))
        );
    }
}
