package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.model.DeactivatedToken;
import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenDeserializer;
import com.softserve.bookstoreapi.service.impl.TokenDeactivationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenDeactivationServiceTest {

    @Mock
    private DeactivatedTokenRepository deactivatedTokenRepository;

    @Mock
    private TokenDeserializer tokenDeserializer;

    @InjectMocks
    private TokenDeactivationService tokenDeactivationService;

    @Captor
    private ArgumentCaptor<DeactivatedToken> deactivatedTokenCaptor;

    private Token validAccessToken;
    private Token validRefreshToken;
    private String validAccessTokenString;

    @BeforeEach
    void setUp() {
        validAccessToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_USER", "ROLE_CUSTOMER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );

        validRefreshToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("REFRESH_TOKEN"),
                Instant.now(),
                Instant.now().plusSeconds(604800)
        );

        validAccessTokenString = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4R0NNIn0..valid.token.string";
    }

    @Test
    void shouldDeactivateValidAccessToken() {
        when(tokenDeserializer.deserialize(validAccessTokenString)).thenReturn(validAccessToken);
        when(deactivatedTokenRepository.save(any(DeactivatedToken.class))).thenAnswer(i -> i.getArgument(0));

        tokenDeactivationService.deactivateAccessToken(validAccessTokenString);

        verify(tokenDeserializer, times(1)).deserialize(validAccessTokenString);
        verify(deactivatedTokenRepository, times(1)).save(deactivatedTokenCaptor.capture());

        DeactivatedToken savedToken = deactivatedTokenCaptor.getValue();
        assertThat(savedToken.getId()).isEqualTo(validAccessToken.tokenId());
        assertThat(savedToken.getKeepUntil()).isEqualTo(validAccessToken.expiresAt());
    }

    @Test
    void shouldDeactivateValidRefreshToken() {
        String refreshTokenString = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4R0NNIn0..refresh.token.string";
        when(tokenDeserializer.deserialize(refreshTokenString)).thenReturn(validRefreshToken);
        when(deactivatedTokenRepository.save(any(DeactivatedToken.class))).thenAnswer(i -> i.getArgument(0));

        tokenDeactivationService.deactivateAccessToken(refreshTokenString);

        verify(tokenDeserializer, times(1)).deserialize(refreshTokenString);
        verify(deactivatedTokenRepository, times(1)).save(deactivatedTokenCaptor.capture());

        DeactivatedToken savedToken = deactivatedTokenCaptor.getValue();
        assertThat(savedToken.getId()).isEqualTo(validRefreshToken.tokenId());
        assertThat(savedToken.getKeepUntil()).isEqualTo(validRefreshToken.expiresAt());
    }

    @Test
    void shouldThrowInvalidJwtTokenWhenDeserializationReturnsNull() {
        when(tokenDeserializer.deserialize(anyString())).thenReturn(null);

        assertThatThrownBy(() -> tokenDeactivationService.deactivateAccessToken(validAccessTokenString))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("Invalid access token format");

        verify(tokenDeserializer, times(1)).deserialize(validAccessTokenString);
        verify(deactivatedTokenRepository, never()).save(any());
    }

    @Test
    void shouldThrowInvalidJwtTokenWhenDeserializationFails() {
        when(tokenDeserializer.deserialize(anyString()))
                .thenThrow(new InvalidJwtToken("error.token.invalid"));

        assertThatThrownBy(() -> tokenDeactivationService.deactivateAccessToken(validAccessTokenString))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("Invalid access token format");

        verify(tokenDeserializer, times(1)).deserialize(validAccessTokenString);
        verify(deactivatedTokenRepository, never()).save(any());
    }

    @Test
    void shouldThrowInvalidJwtTokenForMalformedTokenString() {
        String malformedToken = "not.a.valid.token";
        when(tokenDeserializer.deserialize(malformedToken))
                .thenThrow(new InvalidJwtToken("error.token.invalid"));

        assertThatThrownBy(() -> tokenDeactivationService.deactivateAccessToken(malformedToken))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("Invalid access token format");

        verify(deactivatedTokenRepository, never()).save(any());
    }

    @Test
    void shouldVerifyDatabaseSaveOperationIsCalled() {
        when(tokenDeserializer.deserialize(validAccessTokenString)).thenReturn(validAccessToken);
        when(deactivatedTokenRepository.save(any(DeactivatedToken.class))).thenAnswer(i -> i.getArgument(0));

        tokenDeactivationService.deactivateAccessToken(validAccessTokenString);

        verify(deactivatedTokenRepository, times(1)).save(any(DeactivatedToken.class));
    }

    @Test
    void shouldSaveDeactivatedTokenWithCorrectExpirationTimestamp() {
        Instant expectedExpiry = Instant.now().plusSeconds(900);
        Token token = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_USER"),
                Instant.now(),
                expectedExpiry
        );
        when(tokenDeserializer.deserialize(validAccessTokenString)).thenReturn(token);
        when(deactivatedTokenRepository.save(any(DeactivatedToken.class))).thenAnswer(i -> i.getArgument(0));

        tokenDeactivationService.deactivateAccessToken(validAccessTokenString);

        verify(deactivatedTokenRepository).save(deactivatedTokenCaptor.capture());
        DeactivatedToken savedToken = deactivatedTokenCaptor.getValue();
        assertThat(savedToken.getKeepUntil()).isEqualTo(expectedExpiry);
    }

    @Test
    void shouldSaveDeactivatedTokenWithTokenIdAsEntityId() {
        UUID tokenId = UUID.randomUUID();
        Token token = new Token(
                tokenId,
                "test@example.com",
                List.of("ROLE_USER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );
        when(tokenDeserializer.deserialize(validAccessTokenString)).thenReturn(token);
        when(deactivatedTokenRepository.save(any(DeactivatedToken.class))).thenAnswer(i -> i.getArgument(0));

        tokenDeactivationService.deactivateAccessToken(validAccessTokenString);

        verify(deactivatedTokenRepository).save(deactivatedTokenCaptor.capture());
        DeactivatedToken savedToken = deactivatedTokenCaptor.getValue();
        assertThat(savedToken.getId()).isEqualTo(tokenId);
    }

    @Test
    void shouldHandleTokenWithVeryLongExpiryTime() {
        // Given
        Instant longExpiry = Instant.now().plusSeconds(31536000); // 1 year
        Token longLivedToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("REFRESH_TOKEN"),
                Instant.now(),
                longExpiry
        );
        when(tokenDeserializer.deserialize(validAccessTokenString)).thenReturn(longLivedToken);
        when(deactivatedTokenRepository.save(any(DeactivatedToken.class))).thenAnswer(i -> i.getArgument(0));

        tokenDeactivationService.deactivateAccessToken(validAccessTokenString);

        verify(deactivatedTokenRepository).save(deactivatedTokenCaptor.capture());
        DeactivatedToken savedToken = deactivatedTokenCaptor.getValue();
        assertThat(savedToken.getKeepUntil()).isEqualTo(longExpiry);
    }

    @Test
    void shouldHandleAlreadyExpiredToken() {
        Token expiredToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_USER"),
                Instant.now().minusSeconds(1000),
                Instant.now().minusSeconds(100)
        );
        when(tokenDeserializer.deserialize(validAccessTokenString)).thenReturn(expiredToken);
        when(deactivatedTokenRepository.save(any(DeactivatedToken.class))).thenAnswer(i -> i.getArgument(0));

        tokenDeactivationService.deactivateAccessToken(validAccessTokenString);

        verify(deactivatedTokenRepository).save(deactivatedTokenCaptor.capture());
        DeactivatedToken savedToken = deactivatedTokenCaptor.getValue();
        assertThat(savedToken.getKeepUntil()).isEqualTo(expiredToken.expiresAt());
    }

    @Test
    void shouldHandleMultipleTokenDeactivations() {
        Token token1 = new Token(UUID.randomUUID(), "user1@example.com", List.of("ROLE_USER"), Instant.now(), Instant.now().plusSeconds(900));
        Token token2 = new Token(UUID.randomUUID(), "user2@example.com", List.of("ROLE_USER"), Instant.now(), Instant.now().plusSeconds(900));

        String tokenString1 = "token.string.1";
        String tokenString2 = "token.string.2";

        when(tokenDeserializer.deserialize(tokenString1)).thenReturn(token1);
        when(tokenDeserializer.deserialize(tokenString2)).thenReturn(token2);
        when(deactivatedTokenRepository.save(any(DeactivatedToken.class))).thenAnswer(i -> i.getArgument(0));

        tokenDeactivationService.deactivateAccessToken(tokenString1);
        tokenDeactivationService.deactivateAccessToken(tokenString2);

        verify(deactivatedTokenRepository, times(2)).save(any(DeactivatedToken.class));
    }

    @Test
    void shouldNotSaveIfExceptionOccursDuringDeserialization() {
        when(tokenDeserializer.deserialize(anyString()))
                .thenThrow(new RuntimeException("Deserialization error"));

        assertThatThrownBy(() -> tokenDeactivationService.deactivateAccessToken(validAccessTokenString))
                .isInstanceOf(InvalidJwtToken.class);

        verify(deactivatedTokenRepository, never()).save(any());
    }

    @Test
    void shouldWrapAnyExceptionAsInvalidJwtToken() {
        when(tokenDeserializer.deserialize(anyString()))
                .thenThrow(new NullPointerException("Unexpected error"));

        assertThatThrownBy(() -> tokenDeactivationService.deactivateAccessToken(validAccessTokenString))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessage("Invalid access token format");
    }
}

