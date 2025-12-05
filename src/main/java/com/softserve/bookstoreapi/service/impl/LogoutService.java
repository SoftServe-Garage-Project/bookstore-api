package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.LogoutRequestDTO;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {

    private final TokenDeactivationService tokenDeactivationService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void logout(LogoutRequestDTO request) {
        log.debug("Logout request received");

        // Try to deactivate tokens - if they're invalid, that's ok (user wants to logout anyway)
        try {
            tokenDeactivationService.deactivateAccessToken(request.accessToken());
        } catch (InvalidJwtToken e) {
            log.debug("Logout with invalid access token: {}", e.getMessage());
            // Continue - user still wants to logout
        }

        try {
            refreshTokenService.revokeRefreshTokenByString(request.refreshToken());
        } catch (InvalidJwtToken e) {
            log.debug("Logout with invalid refresh token: {}", e.getMessage());
            // Continue - user still wants to logout
        }

        log.info("User successfully logged out");
    }
}

