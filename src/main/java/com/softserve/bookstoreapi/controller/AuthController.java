package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.AccountDTO;
import com.softserve.bookstoreapi.dto.LoginRequestDTO;
import com.softserve.bookstoreapi.dto.LoginResponseDTO;
import com.softserve.bookstoreapi.dto.LogoutRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshResponseDTO;
import com.softserve.bookstoreapi.exception.TooManyLoginAttemptsException;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.security.CookieUtil;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.LoginAttemptService;
import com.softserve.bookstoreapi.service.impl.LogoutService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
            HttpServletRequest request,
            HttpServletResponse response) {

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
            AccountService.LoginResult loginResult = accountService.login(loginRequest);

            // Set tokens in HTTP-only cookies
            CookieUtil.setAuthenticationCookies(response, loginResult.getAccessToken(), loginResult.getRefreshToken());

            // Clear failed attempts on successful login
            loginAttemptService.loginSucceeded(clientIp);

            return ResponseEntity.ok(loginResult.getResponseDTO());
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

    /**
     * Returns information about the currently authenticated user.
     * Requires valid access token in HTTP-only cookie.
     *
     * @param authentication Current authentication context (populated from access token cookie)
     * @return AccountDTO containing full user information (id, username, email, role, balance, permissions, etc.)
     */
    @GetMapping("/me")
    public ResponseEntity<AccountDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        Account account = accountService.getAccountByEmail(email);

        AccountDTO accountDTO = new AccountDTO(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getRole(),
                account.getBalance(),
                account.getPermissions(),
                account.getIsActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );

        return ResponseEntity.ok(accountDTO);
    }

    /**
     * Refreshes authentication tokens using refresh token from HTTP-only cookie.
     * Generates new access and refresh tokens and sets them as HTTP-only cookies.
     *
     * @param refreshRequest Empty request body (refresh token is read from cookie)
     * @param request HTTP request for reading refresh token cookie
     * @param response HTTP response for setting new authentication cookies
     * @return RefreshResponseDTO with success message (tokens are in cookies)
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDTO> refresh(
            @Valid @RequestBody RefreshRequestDTO refreshRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        RefreshTokenService.RefreshResult refreshResult = refreshTokenService.refreshTokens(request);

        // Set new tokens in HTTP-only cookies
        CookieUtil.setAuthenticationCookies(response, refreshResult.getAccessToken(), refreshResult.getRefreshToken());

        return ResponseEntity.ok(refreshResult.getResponseDTO());
    }

    /**
     * Logs out the current user by deactivating tokens and clearing authentication cookies.
     * Reads access and refresh tokens from HTTP-only cookies, deactivates them in the database,
     * and clears the cookies from the response.
     *
     * @param logoutRequest Empty request body (tokens are read from cookies)
     * @param request HTTP request for reading authentication cookies
     * @param response HTTP response for clearing authentication cookies
     * @return HTTP 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequestDTO logoutRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        logoutService.logout(request);

        // Clear authentication cookies
        CookieUtil.clearAuthenticationCookies(response);

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
