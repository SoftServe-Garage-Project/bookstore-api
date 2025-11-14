package com.softserve.bookstoreapi.security;

import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCookieSessionAuthenticationStrategy implements SessionAuthenticationStrategy {
    private final TokenFactory tokenFactory;
    private final TokenSerializer tokenSerializer;
    private final RefreshTokenService refreshTokenService;
    
    private static final String ACCESS_TOKEN_COOKIE_NAME = "__Host-auth-token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "__Host-refresh-token";
    private static final String SAME_SITE_STRICT = "Strict";

    @Override
    public void onAuthentication(@NotNull Authentication authentication, HttpServletRequest request,
                                 HttpServletResponse response) throws SessionAuthenticationException {

        if (authentication.getName() == null || authentication.getName().isBlank()) {
            throw new SessionAuthenticationException("Authentication principal name is missing");
        }

        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            log.debug("Skipping token creation for non-UsernamePasswordAuthenticationToken: {}",
                    authentication.getClass().getSimpleName());
            return;
        }

        log.debug("Creating authentication tokens for user: {}", authentication.getName());

        try {
            var accessToken = tokenFactory.createAccessToken(authentication);
            var accessTokenString = tokenSerializer.serialize(accessToken);
            var accessCookie = createSecureCookie(
                    ACCESS_TOKEN_COOKIE_NAME,
                    accessTokenString,
                    accessToken.expiresAt()
            );
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

            var refreshToken = tokenFactory.createRefreshToken(authentication);
            var refreshTokenString = tokenSerializer.serialize(refreshToken);
            refreshTokenService.saveRefreshToken(refreshToken);
            var refreshCookie = createSecureCookie(
                    REFRESH_TOKEN_COOKIE_NAME,
                    refreshTokenString,
                    refreshToken.expiresAt()
            );
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            log.info("Successfully created and set authentication cookies for user: {}", authentication.getName());
        } catch (Exception e) {
            log.error("Failed to create authentication cookies for user: {}", authentication.getName(), e);
            throw new SessionAuthenticationException("Failed to create authentication tokens: " + e.getMessage());
        }
    }

    private ResponseCookie createSecureCookie(String name, String value, Instant expiresAt) {
        long maxAgeSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();

        if (maxAgeSeconds <= 0) {
            throw new SessionAuthenticationException("Token already expired before cookie creation");
        }

        if (maxAgeSeconds > Integer.MAX_VALUE) {
            throw new IllegalStateException("Token lifetime exceeds maximum cookie age");
        }

        return ResponseCookie.from(name, value)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .maxAge(maxAgeSeconds)
                .sameSite(SAME_SITE_STRICT)
                .build();
    }
}
