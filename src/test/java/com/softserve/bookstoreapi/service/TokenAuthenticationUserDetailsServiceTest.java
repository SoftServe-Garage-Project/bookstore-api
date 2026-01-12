package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.exception.AccessTokenExpiredException;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.exception.TokenDeactivatedException;
import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenUser;
import com.softserve.bookstoreapi.service.impl.TokenAuthenticationUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationUserDetailsServiceTest {

    @Mock
    private DeactivatedTokenRepository deactivatedTokenRepository;

    @InjectMocks
    private TokenAuthenticationUserDetailsService userDetailsService;

    private Token validToken;
    private PreAuthenticatedAuthenticationToken authenticationToken;

    @BeforeEach
    void setUp() {
        validToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_USER", "ROLE_CUSTOMER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );
        authenticationToken = new PreAuthenticatedAuthenticationToken(validToken, null);
    }

    @Test
    void shouldLoadUserFromValidToken() {
        when(deactivatedTokenRepository.existsById(validToken.tokenId())).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserDetails(authenticationToken);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(TokenUser.class);
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_CUSTOMER");

        verify(deactivatedTokenRepository, times(1)).existsById(validToken.tokenId());
    }

    @Test
    void shouldRejectExpiredToken() {
        Token expiredToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_USER"),
                Instant.now().minusSeconds(1000),
                Instant.now().minusSeconds(100)
        );
        PreAuthenticatedAuthenticationToken expiredAuthToken =
                new PreAuthenticatedAuthenticationToken(expiredToken, null);

        assertThatThrownBy(() -> userDetailsService.loadUserDetails(expiredAuthToken))
                .isInstanceOf(AccessTokenExpiredException.class)
                .hasMessageContaining("Access token has expired");

        verify(deactivatedTokenRepository, never()).existsById(any());
    }

    @Test
    void shouldRejectTokenWithInvalidSubject() {
        Token tokenWithNullSubject = new Token(
                UUID.randomUUID(),
                null,
                List.of("ROLE_USER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );
        PreAuthenticatedAuthenticationToken authToken =
                new PreAuthenticatedAuthenticationToken(tokenWithNullSubject, null);
        when(deactivatedTokenRepository.existsById(any())).thenReturn(false);

        assertThatThrownBy(() -> userDetailsService.loadUserDetails(authToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot pass null or empty values to constructor");
    }

    @Test
    void shouldRejectDeactivatedToken() {
        when(deactivatedTokenRepository.existsById(validToken.tokenId())).thenReturn(true);

        assertThatThrownBy(() -> userDetailsService.loadUserDetails(authenticationToken))
                .isInstanceOf(TokenDeactivatedException.class)
                .hasMessageContaining("Token has been deactivated");

        verify(deactivatedTokenRepository, times(1)).existsById(validToken.tokenId());
    }

    @Test
    void shouldThrowInvalidJwtTokenWhenPrincipalIsNotAToken() {
        PreAuthenticatedAuthenticationToken invalidAuthToken =
                new PreAuthenticatedAuthenticationToken("not-a-token", null);

        assertThatThrownBy(() -> userDetailsService.loadUserDetails(invalidAuthToken))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessageContaining("Invalid token principal type");

        verify(deactivatedTokenRepository, never()).existsById(any());
    }

    @Test
    void shouldReturnTokenUserWithCorrectAuthorities() {
        Token tokenWithMultipleAuthorities = new Token(
                UUID.randomUUID(),
                "admin@example.com",
                List.of("ROLE_ADMIN", "ROLE_USER", "READ_PRIVILEGE", "WRITE_PRIVILEGE"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );
        PreAuthenticatedAuthenticationToken authToken =
                new PreAuthenticatedAuthenticationToken(tokenWithMultipleAuthorities, null);
        when(deactivatedTokenRepository.existsById(any())).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserDetails(authToken);

        assertThat(userDetails.getAuthorities()).hasSize(4);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER", "READ_PRIVILEGE", "WRITE_PRIVILEGE");
    }

    @Test
    void shouldReturnTokenUserWithTokenEmbedded() {
        when(deactivatedTokenRepository.existsById(validToken.tokenId())).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserDetails(authenticationToken);

        assertThat(userDetails).isInstanceOf(TokenUser.class);
        TokenUser tokenUser = (TokenUser) userDetails;
        assertThat(tokenUser.getToken()).isEqualTo(validToken);
    }

    @Test
    void shouldCheckTokenExpirationBeforeCheckingDeactivation() {
        Token expiredToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_USER"),
                Instant.now().minusSeconds(1000),
                Instant.now().minusSeconds(100)
        );
        PreAuthenticatedAuthenticationToken authToken =
                new PreAuthenticatedAuthenticationToken(expiredToken, null);

        assertThatThrownBy(() -> userDetailsService.loadUserDetails(authToken))
                .isInstanceOf(AccessTokenExpiredException.class);

        verify(deactivatedTokenRepository, never()).existsById(any());
    }

    @Test
    void shouldHandleTokenExpiringExactlyNow() {
        Instant now = Instant.now();
        Token tokenExpiringNow = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_USER"),
                now.minusSeconds(900),
                now.minusMillis(1)
        );
        PreAuthenticatedAuthenticationToken authToken =
                new PreAuthenticatedAuthenticationToken(tokenExpiringNow, null);

        assertThatThrownBy(() -> userDetailsService.loadUserDetails(authToken))
                .isInstanceOf(AccessTokenExpiredException.class);
    }

    @Test
    void shouldReturnUserDetailsWithNoPasswordAsPassword() {
        when(deactivatedTokenRepository.existsById(validToken.tokenId())).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserDetails(authenticationToken);

        assertThat(userDetails.getPassword()).isEqualTo("nopassword");
    }

    @Test
    void shouldReturnUserDetailsWithAccountFlagsSetToTrue() {
        when(deactivatedTokenRepository.existsById(validToken.tokenId())).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserDetails(authenticationToken);

        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void shouldHandleTokenWithSingleAuthority() {
        Token tokenWithSingleAuthority = new Token(
                UUID.randomUUID(),
                "user@example.com",
                List.of("ROLE_USER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );
        PreAuthenticatedAuthenticationToken authToken =
                new PreAuthenticatedAuthenticationToken(tokenWithSingleAuthority, null);
        when(deactivatedTokenRepository.existsById(any())).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserDetails(authToken);

        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void shouldHandleRefreshToken() {
        Token refreshToken = new Token(
                UUID.randomUUID(),
                "user@example.com",
                List.of("REFRESH_TOKEN"),
                Instant.now(),
                Instant.now().plusSeconds(604800)
        );
        PreAuthenticatedAuthenticationToken authToken =
                new PreAuthenticatedAuthenticationToken(refreshToken, null);
        when(deactivatedTokenRepository.existsById(any())).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserDetails(authToken);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("REFRESH_TOKEN");
    }

    @Test
    void shouldIncludeTokenIdInExceptionWhenRejectingExpiredToken() {
        UUID tokenId = UUID.randomUUID();
        Token expiredToken = new Token(
                tokenId,
                "test@example.com",
                List.of("ROLE_USER"),
                Instant.now().minusSeconds(1000),
                Instant.now().minusSeconds(100)
        );
        PreAuthenticatedAuthenticationToken authToken =
                new PreAuthenticatedAuthenticationToken(expiredToken, null);

        assertThatThrownBy(() -> userDetailsService.loadUserDetails(authToken))
                .isInstanceOf(AccessTokenExpiredException.class)
                .hasMessageContaining("Access token has expired")
                .extracting("tokenId")
                .isEqualTo(tokenId);
    }

    @Test
    void shouldIncludeTokenIdInExceptionWhenRejectingDeactivatedToken() {
        UUID tokenId = validToken.tokenId();
        when(deactivatedTokenRepository.existsById(tokenId)).thenReturn(true);

        assertThatThrownBy(() -> userDetailsService.loadUserDetails(authenticationToken))
                .isInstanceOf(TokenDeactivatedException.class)
                .hasMessageContaining("Token has been deactivated")
                .extracting("tokenId")
                .isEqualTo(tokenId);
    }
}

