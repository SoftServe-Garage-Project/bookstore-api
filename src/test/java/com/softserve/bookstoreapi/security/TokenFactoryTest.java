package com.softserve.bookstoreapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenFactoryTest {

    private TokenFactory tokenFactory;

    @BeforeEach
    void setUp() {
        tokenFactory = new TokenFactory(Duration.ofMinutes(15), Duration.ofDays(7));
    }

    @Test
    void createPasswordRecoveryToken_Success() {
        String email = "test@example.com";
        Token token = tokenFactory.createPasswordRecoveryToken(email);

        assertNotNull(token);
        assertEquals(email, token.subject());
        assertEquals(List.of("PASSWORD_RECOVERY"), token.authorities());
        assertNotNull(token.tokenId());
        assertNotNull(token.createdAt());
        assertNotNull(token.expiresAt());
        assertTrue(token.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void createPasswordRecoveryToken_NullEmail() {
        assertThrows(IllegalArgumentException.class, () -> tokenFactory.createPasswordRecoveryToken(null));
    }

    @Test
    void createPasswordRecoveryToken_BlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> tokenFactory.createPasswordRecoveryToken(""));
    }
}

