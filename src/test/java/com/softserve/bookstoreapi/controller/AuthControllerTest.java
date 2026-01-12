package com.softserve.bookstoreapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.config.TestSecurityConfig;
import com.softserve.bookstoreapi.dto.LoginRequestDTO;
import com.softserve.bookstoreapi.dto.LoginResponseDTO;
import com.softserve.bookstoreapi.dto.LogoutRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshResponseDTO;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.LoginAttemptService;
import com.softserve.bookstoreapi.service.impl.LogoutService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private LogoutService logoutService;

    @MockitoBean
    private LoginAttemptService loginAttemptService;

    @Test
    void login_Success() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password123");
        LoginResponseDTO loginResponse = new LoginResponseDTO(
                "test@example.com",
                List.of("ROLE_USER"),
                "access-token-value",
                "refresh-token-value"
        );

        when(loginAttemptService.isBlocked(anyString())).thenReturn(false);
        when(accountService.login(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("access-token-value"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-value"));

        verify(accountService, times(1)).login(any(LoginRequestDTO.class));
        verify(loginAttemptService, times(1)).loginSucceeded(anyString());
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "wrongpassword");

        when(loginAttemptService.isBlocked(anyString())).thenReturn(false);
        when(accountService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(accountService, times(1)).login(any(LoginRequestDTO.class));
        verify(loginAttemptService, times(1)).loginFailed(anyString());
    }

    @Test
    void login_ValidationFailed_EmptyEmail() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("", "password123");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).login(any());
    }

    @Test
    void login_ValidationFailed_EmptyPassword() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).login(any());
    }

    @Test
    void refresh_Success() throws Exception {
        RefreshRequestDTO refreshRequest = new RefreshRequestDTO("valid-refresh-token");
        RefreshResponseDTO refreshResponse = new RefreshResponseDTO(
                "new-access-token",
                "new-refresh-token"
        );

        when(refreshTokenService.refreshTokens(any(RefreshRequestDTO.class))).thenReturn(refreshResponse);

        mockMvc.perform(post("/api/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

        verify(refreshTokenService, times(1)).refreshTokens(any(RefreshRequestDTO.class));
    }

    @Test
    void refresh_InvalidToken() throws Exception {
        RefreshRequestDTO refreshRequest = new RefreshRequestDTO("invalid-refresh-token");

        when(refreshTokenService.refreshTokens(any(RefreshRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/api/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isInternalServerError());

        verify(refreshTokenService, times(1)).refreshTokens(any(RefreshRequestDTO.class));
    }

    @Test
    void refresh_ValidationFailed_EmptyToken() throws Exception {
        RefreshRequestDTO refreshRequest = new RefreshRequestDTO("");

        mockMvc.perform(post("/api/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isBadRequest());

        verify(refreshTokenService, never()).refreshTokens(any());
    }

    @Test
    void logout_Success() throws Exception {
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO("valid-access-token", "valid-refresh-token");

        doNothing().when(logoutService).logout(any(LogoutRequestDTO.class));

        mockMvc.perform(post("/api/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        verify(logoutService, times(1)).logout(any(LogoutRequestDTO.class));
    }

    @Test
    void logout_TokenDeactivationError() throws Exception {
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO("valid-access-token", "valid-refresh-token");

        doThrow(new RuntimeException("Token deactivation failed"))
                .when(logoutService).logout(any(LogoutRequestDTO.class));

        mockMvc.perform(post("/api/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isInternalServerError());

        verify(logoutService, times(1)).logout(any(LogoutRequestDTO.class));
    }

    @Test
    void logout_ValidationFailed_EmptyToken() throws Exception {
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO("", "");

        mockMvc.perform(post("/api/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isBadRequest());

        verify(logoutService, never()).logout(any());
    }

    @Test
    void login_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).login(any());
    }

    @Test
    void refresh_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(refreshTokenService, never()).refreshTokens(any());
    }

    @Test
    void logout_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(logoutService, never()).logout(any());
    }
}

