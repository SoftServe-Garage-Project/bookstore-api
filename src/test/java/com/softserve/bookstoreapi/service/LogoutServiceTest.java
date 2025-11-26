package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.LogoutRequestDTO;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.service.impl.LogoutService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import com.softserve.bookstoreapi.service.impl.TokenDeactivationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutService Unit Tests")
class LogoutServiceTest {

    @Mock
    private TokenDeactivationService tokenDeactivationService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    @DisplayName("Should logout successfully with valid tokens")
    void logout_ValidTokens_DeactivatesSuccessfully() {
        // Given
        LogoutRequestDTO request = new LogoutRequestDTO("validAccessToken", "validRefreshToken");
        doNothing().when(tokenDeactivationService).deactivateAccessToken("validAccessToken");
        doNothing().when(refreshTokenService).revokeRefreshTokenByString("validRefreshToken");

        // When & Then
        assertDoesNotThrow(() -> logoutService.logout(request));

        verify(tokenDeactivationService).deactivateAccessToken("validAccessToken");
        verify(refreshTokenService).revokeRefreshTokenByString("validRefreshToken");
    }

    @Test
    @DisplayName("Should handle invalid access token gracefully during logout")
    void logout_InvalidAccessToken_HandlesGracefully() {
        // Given
        LogoutRequestDTO request = new LogoutRequestDTO("invalidAccessToken", "validRefreshToken");
        doThrow(new InvalidJwtToken("Invalid access token"))
                .when(tokenDeactivationService).deactivateAccessToken("invalidAccessToken");
        doNothing().when(refreshTokenService).revokeRefreshTokenByString("validRefreshToken");

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> logoutService.logout(request));

        verify(tokenDeactivationService).deactivateAccessToken("invalidAccessToken");
        verify(refreshTokenService).revokeRefreshTokenByString("validRefreshToken");
    }

    @Test
    @DisplayName("Should handle invalid refresh token gracefully during logout")
    void logout_InvalidRefreshToken_HandlesGracefully() {
        // Given
        LogoutRequestDTO request = new LogoutRequestDTO("validAccessToken", "invalidRefreshToken");
        doNothing().when(tokenDeactivationService).deactivateAccessToken("validAccessToken");
        doThrow(new InvalidJwtToken("Invalid refresh token"))
                .when(refreshTokenService).revokeRefreshTokenByString("invalidRefreshToken");

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> logoutService.logout(request));

        verify(tokenDeactivationService).deactivateAccessToken("validAccessToken");
        verify(refreshTokenService).revokeRefreshTokenByString("invalidRefreshToken");
    }

    @Test
    @DisplayName("Should handle both invalid tokens gracefully during logout")
    void logout_BothTokensInvalid_HandlesGracefully() {
        // Given
        LogoutRequestDTO request = new LogoutRequestDTO("invalidAccessToken", "invalidRefreshToken");
        doThrow(new InvalidJwtToken("Invalid access token"))
                .when(tokenDeactivationService).deactivateAccessToken("invalidAccessToken");
        doThrow(new InvalidJwtToken("Invalid refresh token"))
                .when(refreshTokenService).revokeRefreshTokenByString("invalidRefreshToken");

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> logoutService.logout(request));

        verify(tokenDeactivationService).deactivateAccessToken("invalidAccessToken");
        verify(refreshTokenService).revokeRefreshTokenByString("invalidRefreshToken");
    }
}

