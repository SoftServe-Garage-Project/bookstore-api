package com.softserve.bookstoreapi.security;

import jakarta.servlet.http.Cookie;
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
    private TokenDeserializer tokenDeserializer;

    @Mock
    private HttpServletRequest request;

    private CookieTokenAuthenticationConverter converter;
    private Token validToken;

    @BeforeEach
    void setUp() {
        converter = new CookieTokenAuthenticationConverter(tokenDeserializer);

        validToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_CUSTOMER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );
    }

    @Test
    void convert_ValidAccessTokenCookie_ReturnsAuthentication() {
        String tokenValue = "validJwtToken";
        Cookie accessTokenCookie = new Cookie("accessToken", tokenValue);
        Cookie[] cookies = {accessTokenCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(tokenDeserializer.deserialize(tokenValue)).thenReturn(validToken);

        Authentication result = converter.convert(request);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PreAuthenticatedAuthenticationToken.class);
        assertThat(result.getPrincipal()).isEqualTo(validToken);
        assertThat(result.getCredentials()).isEqualTo(tokenValue);
    }

    @Test
    void convert_NoCookies_ReturnsNull() {
        when(request.getCookies()).thenReturn(null);

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_NoAccessTokenCookie_ReturnsNull() {
        Cookie otherCookie = new Cookie("otherCookie", "somevalue");
        Cookie[] cookies = {otherCookie};

        when(request.getCookies()).thenReturn(cookies);

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_EmptyTokenValue_ReturnsNull() {
        Cookie accessTokenCookie = new Cookie("accessToken", "");
        Cookie[] cookies = {accessTokenCookie};

        when(request.getCookies()).thenReturn(cookies);

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_InvalidToken_ReturnsNull() {
        String tokenValue = "invalidToken";
        Cookie accessTokenCookie = new Cookie("accessToken", tokenValue);
        Cookie[] cookies = {accessTokenCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(tokenDeserializer.deserialize(tokenValue)).thenThrow(new RuntimeException("Invalid token"));

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_DeserializerReturnsNull_ReturnsNull() {
        String tokenValue = "someToken";
        Cookie accessTokenCookie = new Cookie("accessToken", tokenValue);
        Cookie[] cookies = {accessTokenCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(tokenDeserializer.deserialize(tokenValue)).thenReturn(null);

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_MultipleIncludingAccessToken_ReturnsAuthentication() {
        String tokenValue = "validJwtToken";
        Cookie cookie1 = new Cookie("sessionId", "xyz123");
        Cookie accessTokenCookie = new Cookie("accessToken", tokenValue);
        Cookie cookie2 = new Cookie("preferences", "theme=dark");
        Cookie[] cookies = {cookie1, accessTokenCookie, cookie2};

        when(request.getCookies()).thenReturn(cookies);
        when(tokenDeserializer.deserialize(tokenValue)).thenReturn(validToken);

        Authentication result = converter.convert(request);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PreAuthenticatedAuthenticationToken.class);
        assertThat(result.getPrincipal()).isEqualTo(validToken);
    }
}

