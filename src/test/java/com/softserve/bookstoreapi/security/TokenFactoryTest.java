package com.softserve.bookstoreapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TokenFactoryTest {

    private TokenFactory tokenFactory;
    private Authentication mockAuthentication;
    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

    @BeforeEach
    void setUp() {
        tokenFactory = new TokenFactory(ACCESS_TOKEN_DURATION, REFRESH_TOKEN_DURATION);
        mockAuthentication = mock(Authentication.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateAccessTokenWithValidUserDetails() {
        String email = "test@example.com";
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_CUSTOMER")
        );
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn((Collection) authorities);

        Instant beforeCreation = Instant.now();

        Token token = tokenFactory.createAccessToken(mockAuthentication);

        assertThat(token).isNotNull();
        assertThat(token.tokenId()).isNotNull();
        assertThat(token.subject()).isEqualTo(email);
        assertThat(token.authorities()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_CUSTOMER");
        assertThat(token.createdAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(token.expiresAt()).isAfter(token.createdAt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateRefreshTokenWithValidUserDetails() {
        String email = "test@example.com";
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn((Collection) authorities);

        Instant beforeCreation = Instant.now();

        Token token = tokenFactory.createRefreshToken(mockAuthentication);

        assertThat(token).isNotNull();
        assertThat(token.tokenId()).isNotNull();
        assertThat(token.subject()).isEqualTo(email);
        assertThat(token.authorities()).containsExactly("REFRESH_TOKEN");
        assertThat(token.createdAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(token.expiresAt()).isAfter(token.createdAt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateAccessTokenWithCorrectExpirationTime() {
        String email = "test@example.com";
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn((Collection) authorities);

        Token token = tokenFactory.createAccessToken(mockAuthentication);

        Duration actualDuration = Duration.between(token.createdAt(), token.expiresAt());
        assertThat(actualDuration.toMinutes()).isEqualTo(ACCESS_TOKEN_DURATION.toMinutes());
    }

    @Test
    void shouldCreateRefreshTokenWithCorrectExpirationTime() {
        String email = "test@example.com";
        when(mockAuthentication.getName()).thenReturn(email);

        Token token = tokenFactory.createRefreshToken(mockAuthentication);

        Duration actualDuration = Duration.between(token.createdAt(), token.expiresAt());
        assertThat(actualDuration.toDays()).isEqualTo(REFRESH_TOKEN_DURATION.toDays());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGenerateUniqueTokenIds() {
        String email = "test@example.com";
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn((Collection) authorities);

        Token token1 = tokenFactory.createAccessToken(mockAuthentication);
        Token token2 = tokenFactory.createAccessToken(mockAuthentication);

        assertThat(token1.tokenId()).isNotEqualTo(token2.tokenId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldVerifyTokenIncludesCorrectAuthorities() {
        String email = "test@example.com";
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_MODERATOR"),
                new SimpleGrantedAuthority("READ_PRIVILEGE")
        );
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn((Collection) authorities);

        Token token = tokenFactory.createAccessToken(mockAuthentication);

        assertThat(token.authorities()).hasSize(3);
        assertThat(token.authorities()).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_MODERATOR", "READ_PRIVILEGE");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldVerifyIssuedAndExpiryTimestampsAreCorrect() {
        String email = "test@example.com";
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn((Collection) authorities);

        Instant before = Instant.now();

        Token token = tokenFactory.createAccessToken(mockAuthentication);

        Instant after = Instant.now();

        assertThat(token.createdAt()).isBetween(before, after);
        assertThat(token.expiresAt()).isAfter(token.createdAt());

        long secondsDiff = Duration.between(token.createdAt(), token.expiresAt()).getSeconds();
        assertThat(secondsDiff).isEqualTo(ACCESS_TOKEN_DURATION.getSeconds());
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationIsNullForAccessToken() {
        assertThatThrownBy(() -> tokenFactory.createAccessToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authentication cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationIsNullForRefreshToken() {
        assertThatThrownBy(() -> tokenFactory.createRefreshToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authentication cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationNameIsNull() {
        when(mockAuthentication.getName()).thenReturn(null);

        assertThatThrownBy(() -> tokenFactory.createAccessToken(mockAuthentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authentication name cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationNameIsBlank() {
        when(mockAuthentication.getName()).thenReturn("   ");

        assertThatThrownBy(() -> tokenFactory.createAccessToken(mockAuthentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authentication name cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenAuthoritiesAreNull() {
        String email = "test@example.com";
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn(null);

        assertThatThrownBy(() -> tokenFactory.createAccessToken(mockAuthentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no authorities assigned");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowExceptionWhenAuthoritiesAreEmpty() {
        String email = "test@example.com";
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn((Collection) List.of());

        assertThatThrownBy(() -> tokenFactory.createAccessToken(mockAuthentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no authorities assigned");
    }

    @Test
    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    void shouldFilterOutNullAuthorities() {
        String email = "test@example.com";
        List<GrantedAuthority> authoritiesWithNull = new ArrayList<>();
        authoritiesWithNull.add(new SimpleGrantedAuthority("ROLE_USER"));
        authoritiesWithNull.add(null);
        authoritiesWithNull.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn((Collection) authoritiesWithNull);

        Token token = tokenFactory.createAccessToken(mockAuthentication);

        assertThat(token.authorities()).hasSize(2);
        assertThat(token.authorities()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFilterOutBlankAuthorities() {
        String email = "test@example.com";
        GrantedAuthority blankAuthority = () -> "   ";
        List<GrantedAuthority> authoritiesWithBlank = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                blankAuthority,
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn((Collection) authoritiesWithBlank);

        Token token = tokenFactory.createAccessToken(mockAuthentication);

        assertThat(token.authorities()).hasSize(2);
        assertThat(token.authorities()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowExceptionWhenAllAuthoritiesAreInvalid() {
        String email = "test@example.com";
        GrantedAuthority blankAuthority = () -> "   ";
        GrantedAuthority nullAuthority = () -> null;
        List<GrantedAuthority> invalidAuthorities = List.of(blankAuthority, nullAuthority);
        when(mockAuthentication.getName()).thenReturn(email);
        when(mockAuthentication.getAuthorities()).thenReturn((Collection) invalidAuthorities);

        assertThatThrownBy(() -> tokenFactory.createAccessToken(mockAuthentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("all authorities are invalid");
    }

    @Test
    void shouldCreateRefreshTokenWithoutRequiringAuthorities() {
        String email = "test@example.com";
        when(mockAuthentication.getName()).thenReturn(email);

        Token token = tokenFactory.createRefreshToken(mockAuthentication);

        assertThat(token).isNotNull();
        assertThat(token.subject()).isEqualTo(email);
        assertThat(token.authorities()).containsExactly("REFRESH_TOKEN");
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

