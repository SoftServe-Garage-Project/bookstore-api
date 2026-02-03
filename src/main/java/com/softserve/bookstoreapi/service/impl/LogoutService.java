package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.security.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {

    private final TokenDeactivationService tokenDeactivationService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Logs out user by deactivating access token and revoking refresh token.
     * Reads tokens from HTTP-only cookies. If tokens are invalid or missing,
     * the operation continues gracefully (user wants to logout anyway).
     *
     * @param request HTTP request containing access and refresh tokens in HTTP-only cookies
     */
    @Transactional
    public void logout(HttpServletRequest request) {
        log.debug("Logout request received");

        // Get tokens from cookies
        Optional<String> accessTokenOpt = CookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE_NAME);
        Optional<String> refreshTokenOpt = CookieUtil.getCookieValue(request, CookieUtil.REFRESH_TOKEN_COOKIE_NAME);

        // Try to deactivate access token - if it's invalid, that's ok (user wants to logout anyway)
        accessTokenOpt.ifPresent(accessToken -> {
            try {
                tokenDeactivationService.deactivateAccessToken(accessToken);
            } catch (InvalidJwtToken e) {
                log.debug("Logout with invalid access token: {}", e.getMessage());
                // Continue - user still wants to logout
            }
        });

        // Try to revoke refresh token
        refreshTokenOpt.ifPresent(refreshToken -> {
            try {
                refreshTokenService.revokeRefreshTokenByString(refreshToken);
            } catch (InvalidJwtToken e) {
                log.debug("Logout with invalid refresh token: {}", e.getMessage());
                // Continue - user still wants to logout
            }
        });

        log.info("User successfully logged out");
    }
}

