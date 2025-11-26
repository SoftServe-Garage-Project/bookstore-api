package com.softserve.bookstoreapi.security;

import com.softserve.bookstoreapi.security.BearerTokenAuthenticationConverter;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenCookieJweStringDeserializer;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BearerTokenAuthenticationConverter Unit Tests")
class BearerTokenAuthenticationConverterTest {

    @Mock
    private TokenCookieJweStringDeserializer tokenDeserializer;

    @Mock
    private HttpServletRequest request;

    private BearerTokenAuthenticationConverter converter;
    private Token validToken;

    @BeforeEach
    void setUp() {
        converter = new BearerTokenAuthenticationConverter(tokenDeserializer);

        validToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_CUSTOMER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );
    }

    @Test
    @DisplayName("Should convert valid Bearer token to Authentication")
    void convert_ValidBearerToken_ReturnsAuthentication() {
        // Given
        String tokenValue = "validJwtToken";
        String authHeader = "Bearer " + tokenValue;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenDeserializer.deserialize(tokenValue)).thenReturn(validToken);

        // When
        Authentication result = converter.convert(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PreAuthenticatedAuthenticationToken.class);
        assertThat(result.getPrincipal()).isEqualTo(validToken);
        assertThat(result.getCredentials()).isEqualTo(tokenValue);
    }

    @Test
    @DisplayName("Should return null when no Authorization header present")
    void convert_NoAuthorizationHeader_ReturnsNull() {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        Authentication result = converter.convert(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when Authorization header doesn't start with Bearer")
    void convert_NoBearerPrefix_ReturnsNull() {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // When
        Authentication result = converter.convert(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when Bearer token is empty")
    void convert_EmptyToken_ReturnsNull() {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // When
        Authentication result = converter.convert(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when Bearer token is only whitespace")
    void convert_WhitespaceToken_ReturnsNull() {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        // When
        Authentication result = converter.convert(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when token deserialization fails")
    void convert_InvalidToken_ReturnsNull() {
        // Given
        String tokenValue = "invalidToken";
        String authHeader = "Bearer " + tokenValue;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenDeserializer.deserialize(tokenValue)).thenThrow(new RuntimeException("Invalid token"));

        // When
        Authentication result = converter.convert(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when token deserialization returns null")
    void convert_DeserializationReturnsNull_ReturnsNull() {
        // Given
        String tokenValue = "someToken";
        String authHeader = "Bearer " + tokenValue;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenDeserializer.deserialize(tokenValue)).thenReturn(null);

        // When
        Authentication result = converter.convert(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle Bearer token with extra whitespace")
    void convert_TokenWithWhitespace_ReturnsAuthentication() {
        // Given
        String tokenValue = "validJwtToken";
        String authHeader = "Bearer   " + tokenValue + "   ";
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenDeserializer.deserialize(tokenValue)).thenReturn(validToken);

        // When
        Authentication result = converter.convert(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrincipal()).isEqualTo(validToken);
    }
}

