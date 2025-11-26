package com.softserve.bookstoreapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.config.TestSecurityConfig;
import com.softserve.bookstoreapi.dto.*;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.LogoutService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private LogoutService logoutService;

    private LoginRequestDTO validLoginRequest;
    private LoginResponseDTO validLoginResponse;
    private RefreshRequestDTO validRefreshRequest;
    private RefreshResponseDTO validRefreshResponse;
    private LogoutRequestDTO validLogoutRequest;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequestDTO(
                "test@example.com",
                "password123"
        );

        validLoginResponse = new LoginResponseDTO(
                "test@example.com",
                List.of("ROLE_CUSTOMER"),
                "accessTokenString",
                "refreshTokenString"
        );

        validRefreshRequest = new RefreshRequestDTO("validRefreshToken");

        validRefreshResponse = new RefreshResponseDTO(
                "newAccessToken",
                "newRefreshToken"
        );

        validLogoutRequest = new LogoutRequestDTO(
                "accessToken",
                "refreshToken"
        );
    }

    @Test
    @WithMockUser
    @DisplayName("Should login successfully with valid credentials")
    void login_ValidCredentials_Returns200WithTokens() throws Exception {
        // Given
        when(accountService.login(any(LoginRequestDTO.class))).thenReturn(validLoginResponse);

        // When & Then
        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.accessToken").value("accessTokenString"))
                .andExpect(jsonPath("$.refreshToken").value("refreshTokenString"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CUSTOMER"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 401 for invalid credentials")
    void login_InvalidCredentials_Returns401() throws Exception {
        // Given
        when(accountService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("error.auth.invalid.credentials"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 for missing email")
    void login_MissingEmail_Returns400() throws Exception {
        // Given
        LoginRequestDTO invalidRequest = new LoginRequestDTO("", "password123");

        // When & Then
        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 for missing password")
    void login_MissingPassword_Returns400() throws Exception {
        // Given
        LoginRequestDTO invalidRequest = new LoginRequestDTO("test@example.com", "");

        // When & Then
        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 for invalid email format")
    void login_InvalidEmailFormat_Returns400() throws Exception {
        // Given
        LoginRequestDTO invalidRequest = new LoginRequestDTO("not-an-email", "password123");

        // When & Then
        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should refresh tokens successfully")
    void refresh_ValidToken_Returns200WithNewTokens() throws Exception {
        // Given
        when(refreshTokenService.refreshTokens(any(RefreshRequestDTO.class)))
                .thenReturn(validRefreshResponse);

        // When & Then
        mockMvc.perform(post("/api/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRefreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 for missing refresh token")
    void refresh_MissingToken_Returns400() throws Exception {
        // Given
        RefreshRequestDTO invalidRequest = new RefreshRequestDTO("");

        // When & Then
        mockMvc.perform(post("/api/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should logout successfully")
    void logout_ValidTokens_Returns204() throws Exception {
        // Given
        doNothing().when(logoutService).logout(any(LogoutRequestDTO.class));

        // When & Then
        mockMvc.perform(post("/api/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogoutRequest)))
                .andExpect(status().isNoContent());

        verify(logoutService, times(1)).logout(any(LogoutRequestDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 for logout with missing access token")
    void logout_MissingAccessToken_Returns400() throws Exception {
        // Given
        LogoutRequestDTO invalidRequest = new LogoutRequestDTO("", "refreshToken");

        // When & Then
        mockMvc.perform(post("/api/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 for logout with missing refresh token")
    void logout_MissingRefreshToken_Returns400() throws Exception {
        // Given
        LogoutRequestDTO invalidRequest = new LogoutRequestDTO("accessToken", "");

        // When & Then
        mockMvc.perform(post("/api/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}

