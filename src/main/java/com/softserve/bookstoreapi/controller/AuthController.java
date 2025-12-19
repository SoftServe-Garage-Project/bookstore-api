package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.LoginRequestDTO;
import com.softserve.bookstoreapi.dto.LoginResponseDTO;
import com.softserve.bookstoreapi.dto.LogoutRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshResponseDTO;
import com.softserve.bookstoreapi.exception.TooManyLoginAttemptsException;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.LoginAttemptService;
import com.softserve.bookstoreapi.service.impl.LogoutService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;
    private final RefreshTokenService refreshTokenService;
    private final LogoutService logoutService;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest request) {

        String clientIp = getClientIP(request);

        // Check if IP is blocked
        if (loginAttemptService.isBlocked(clientIp)) {
            LocalDateTime blockedUntil = loginAttemptService.getBlockExpirationTime(clientIp);
            throw new TooManyLoginAttemptsException(
                    "Too many failed login attempts. Please try again later.",
                    clientIp,
                    blockedUntil
            );
        }

        try {
            LoginResponseDTO responseDTO = accountService.login(loginRequest);
            // Clear failed attempts on successful login
            loginAttemptService.loginSucceeded(clientIp);
            return ResponseEntity.ok(responseDTO);
        } catch (BadCredentialsException e) {
            // Record failed attempt
            loginAttemptService.loginFailed(clientIp);

            // Check if now blocked after this failure
            if (loginAttemptService.isBlocked(clientIp)) {
                LocalDateTime blockedUntil = loginAttemptService.getBlockExpirationTime(clientIp);
                throw new TooManyLoginAttemptsException(
                        "Too many failed login attempts. Account temporarily blocked.",
                        clientIp,
                        blockedUntil
                );
            }

            // Re-throw with remaining attempts info
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDTO> refresh(
            @Valid @RequestBody RefreshRequestDTO refreshRequest) {

        RefreshResponseDTO responseDTO = refreshTokenService.refreshTokens(refreshRequest);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequestDTO logoutRequest) {

        logoutService.logout(logoutRequest);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extracts the client IP address from the request.
     * Handles proxy headers like X-Forwarded-For.
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
