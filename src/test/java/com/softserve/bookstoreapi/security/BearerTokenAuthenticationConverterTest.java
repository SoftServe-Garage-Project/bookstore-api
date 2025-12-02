package com.softserve.bookstoreapi.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
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
    void convert_ValidBearerToken_ReturnsAuthentication() {
        String tokenValue = "validJwtToken";
        String authHeader = "Bearer " + tokenValue;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenDeserializer.deserialize(tokenValue)).thenReturn(validToken);

        Authentication result = converter.convert(request);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PreAuthenticatedAuthenticationToken.class);
        assertThat(result.getPrincipal()).isEqualTo(validToken);
        assertThat(result.getCredentials()).isEqualTo(tokenValue);
    }

    @Test
    void convert_NoAuthorizationHeader_ReturnsNull() {
        when(request.getHeader("Authorization")).thenReturn(null);

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_NoBearerPrefix_ReturnsNull() {
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_EmptyToken_ReturnsNull() {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_WhitespaceToken_ReturnsNull() {
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_InvalidToken_ReturnsNull() {
        String tokenValue = "invalidToken";
        String authHeader = "Bearer " + tokenValue;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenDeserializer.deserialize(tokenValue)).thenThrow(new RuntimeException("Invalid token"));

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_DeserializationReturnsNull_ReturnsNull() {
        String tokenValue = "someToken";
        String authHeader = "Bearer " + tokenValue;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenDeserializer.deserialize(tokenValue)).thenReturn(null);

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_TokenWithWhitespace_ReturnsAuthentication() {
        String tokenValue = "validJwtToken";
        String authHeader = "Bearer   " + tokenValue + "   ";
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenDeserializer.deserialize(tokenValue)).thenReturn(validToken);

        Authentication result = converter.convert(request);

        assertThat(result).isNotNull();
        assertThat(result.getPrincipal()).isEqualTo(validToken);
    }
}

