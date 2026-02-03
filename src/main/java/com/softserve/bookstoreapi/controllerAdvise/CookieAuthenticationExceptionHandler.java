package com.softserve.bookstoreapi.controllerAdvise;

import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.exception.RefreshTokenExpiredException;
import com.softserve.bookstoreapi.exception.RefreshTokenInvalidException;
import com.softserve.bookstoreapi.exception.AccessTokenExpiredException;
import com.softserve.bookstoreapi.exception.TokenDeactivatedException;
import com.softserve.bookstoreapi.security.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Global exception handler for cookie-based authentication errors.
 * Automatically clears authentication cookies when token-related exceptions occur.
 */
@ControllerAdvice
@Slf4j
public class CookieAuthenticationExceptionHandler {

    /**
     * Handles invalid JWT token exceptions.
     * Clears authentication cookies and returns 401 Unauthorized.
     */
    @ExceptionHandler(InvalidJwtToken.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(
            InvalidJwtToken ex,
            HttpServletResponse response) {
        log.warn("Invalid JWT token: {}", ex.getMessage());
        CookieUtil.clearAuthenticationCookies(response);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "error", "invalid_token",
                        "message", ex.getMessage()
                ));
    }

    /**
     * Handles expired access token exceptions.
     * Clears authentication cookies and returns 401 Unauthorized with specific error code.
     */
    @ExceptionHandler(AccessTokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleAccessTokenExpired(
            AccessTokenExpiredException ex,
            HttpServletResponse response) {
        log.debug("Access token expired: {}", ex.getMessage());
        CookieUtil.clearAuthenticationCookies(response);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "error", "access_token_expired",
                        "message", "Access token has expired. Please refresh or login again.",
                        "tokenId", ex.getTokenId() != null ? ex.getTokenId().toString() : "unknown"
                ));
    }

    /**
     * Handles expired refresh token exceptions.
     * Clears authentication cookies and returns 401 Unauthorized.
     */
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleRefreshTokenExpired(
            RefreshTokenExpiredException ex,
            HttpServletResponse response) {
        log.info("Refresh token expired: {}", ex.getMessage());
        CookieUtil.clearAuthenticationCookies(response);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "error", "refresh_token_expired",
                        "message", "Refresh token has expired. Please login again.",
                        "tokenId", ex.getTokenId() != null ? ex.getTokenId().toString() : "unknown"
                ));
    }

    /**
     * Handles invalid refresh token exceptions (already used or revoked).
     * Clears authentication cookies and returns 401 Unauthorized.
     */
    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<Map<String, Object>> handleRefreshTokenInvalid(
            RefreshTokenInvalidException ex,
            HttpServletResponse response) {
        log.warn("Refresh token invalid: {} - {}", ex.getReason(), ex.getMessage());
        CookieUtil.clearAuthenticationCookies(response);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "error", "refresh_token_invalid",
                        "message", ex.getMessage(),
                        "reason", ex.getReason(),
                        "tokenId", ex.getTokenId() != null ? ex.getTokenId().toString() : "unknown"
                ));
    }

    /**
     * Handles deactivated token exceptions.
     * Clears authentication cookies and returns 401 Unauthorized.
     */
    @ExceptionHandler(TokenDeactivatedException.class)
    public ResponseEntity<Map<String, Object>> handleTokenDeactivated(
            TokenDeactivatedException ex,
            HttpServletResponse response) {
        log.warn("Token deactivated: {}", ex.getMessage());
        CookieUtil.clearAuthenticationCookies(response);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "error", "token_deactivated",
                        "message", "Token has been deactivated. Please login again.",
                        "tokenId", ex.getTokenId() != null ? ex.getTokenId().toString() : "unknown"
                ));
    }
}
