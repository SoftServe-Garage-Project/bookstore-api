package com.softserve.bookstoreapi.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class CookieUtil {

    private CookieUtil() {
        // Utility class - prevent instantiation
    }

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    // 30 minutes for access token
    private static final int ACCESS_TOKEN_MAX_AGE = 30 * 60;
    // 8 hours for refresh token
    private static final int REFRESH_TOKEN_MAX_AGE = 8 * 60 * 60;

    /**
     * Create HTTP-only secure cookie for access token
     */
    public static Cookie createAccessTokenCookie(String tokenValue) {
        log.trace("Creating access token cookie");
        return createCookie(ACCESS_TOKEN_COOKIE_NAME, tokenValue, ACCESS_TOKEN_MAX_AGE);
    }

    /**
     * Create HTTP-only secure cookie for refresh token
     */
    public static Cookie createRefreshTokenCookie(String tokenValue) {
        log.trace("Creating refresh token cookie");
        return createCookie(REFRESH_TOKEN_COOKIE_NAME, tokenValue, REFRESH_TOKEN_MAX_AGE);
    }

    /**
     * Create HTTP-only secure cookie
     */
    private static Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS only
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Strict");
        log.debug("Created cookie: name={}, maxAge={}s, httpOnly=true, secure=true, sameSite=Strict",
                name, maxAge);
        return cookie;
    }

    /**
     * Get cookie value by name from request
     */
    public static Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            log.trace("No cookies found in request for cookie name: {}", cookieName);
            return Optional.empty();
        }

        Optional<String> cookieValue = Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();

        if (cookieValue.isPresent()) {
            log.debug("Cookie found: name={}, from IP={}, User-Agent={}",
                    cookieName,
                    getClientIP(request),
                    request.getHeader("User-Agent"));
        } else {
            log.trace("Cookie not found: name={}", cookieName);
        }

        return cookieValue;
    }

    /**
     * Delete cookie by setting maxAge to 0
     */
    public static Cookie createDeleteCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        log.debug("Created delete cookie: name={}", name);
        return cookie;
    }

    /**
     * Clear authentication cookies
     */
    public static void clearAuthenticationCookies(HttpServletResponse response) {
        response.addCookie(createDeleteCookie(ACCESS_TOKEN_COOKIE_NAME));
        response.addCookie(createDeleteCookie(REFRESH_TOKEN_COOKIE_NAME));
        log.info("Cleared authentication cookies: {} and {}",
                ACCESS_TOKEN_COOKIE_NAME, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * Set authentication cookies
     */
    public static void setAuthenticationCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addCookie(createAccessTokenCookie(accessToken));
        response.addCookie(createRefreshTokenCookie(refreshToken));
        log.info("Set authentication cookies: {} and {}",
                ACCESS_TOKEN_COOKIE_NAME, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * Set authentication cookies with request context for logging
     */
    public static void setAuthenticationCookies(HttpServletRequest request, HttpServletResponse response,
                                               String accessToken, String refreshToken) {
        response.addCookie(createAccessTokenCookie(accessToken));
        response.addCookie(createRefreshTokenCookie(refreshToken));
        log.info("Set authentication cookies for IP={}, User-Agent={}",
                getClientIP(request),
                request.getHeader("User-Agent"));
    }

    /**
     * Extract client IP address from request (handles proxy headers)
     */
    private static String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
