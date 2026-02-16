package com.softserve.bookstoreapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.config.TestSecurityConfig;
import com.softserve.bookstoreapi.dto.LoginRequestDTO;
import com.softserve.bookstoreapi.dto.LoginResponseDTO;
import com.softserve.bookstoreapi.dto.LogoutRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshResponseDTO;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.LoginAttemptService;
import com.softserve.bookstoreapi.service.impl.LogoutService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for AuthController with HTTP-only cookie authentication.
 * Updated to test cookie-based token storage instead of JSON response tokens.
 */
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerCookieTest {

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
    void login_Success_SetsCookies() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password123");
        LoginResponseDTO loginResponse = new LoginResponseDTO(
                "test@example.com",
                List.of("ROLE_USER")
        );

        AccountService.LoginResult loginResult = new AccountService.LoginResult(
                loginResponse,
                "access-token-value",
                "refresh-token-value"
        );

        when(loginAttemptService.isBlocked(anyString())).thenReturn(false);
        when(accountService.login(any(LoginRequestDTO.class))).thenReturn(loginResult);

        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        // Verify cookies are set
        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).isNotNull();
        assertThat(cookies).hasSize(2);

        Cookie accessTokenCookie = findCookie(cookies, "accessToken");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isEqualTo("access-token-value");
        assertThat(accessTokenCookie.isHttpOnly()).isTrue();
        assertThat(accessTokenCookie.getSecure()).isTrue();
        assertThat(accessTokenCookie.getMaxAge()).isEqualTo(1800); // 30 minutes

        Cookie refreshTokenCookie = findCookie(cookies, "refreshToken");
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(refreshTokenCookie.getValue()).isEqualTo("refresh-token-value");
        assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
        assertThat(refreshTokenCookie.getSecure()).isTrue();
        assertThat(refreshTokenCookie.getMaxAge()).isEqualTo(28800); // 8 hours

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
    @WithMockUser(username = "test@example.com", roles = {"CUSTOMER"})
    void getCurrentUser_Success() throws Exception {
        String email = "test@example.com";
        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setUsername("testuser");
        mockAccount.setEmail(email);
        mockAccount.setRole(UserRole.ROLE_CUSTOMER);
        mockAccount.setBalance(new BigDecimal("100.00"));
        mockAccount.setPermissions(new ArrayList<>());
        mockAccount.setIsActive(true);
        mockAccount.setCreatedAt(LocalDateTime.now());
        mockAccount.setUpdatedAt(LocalDateTime.now());

        when(accountService.getAccountByEmail(email)).thenReturn(mockAccount);

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"))
                .andExpect(jsonPath("$.balance").value(100.00))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(accountService, times(1)).getAccountByEmail(email);
    }

    @Test
    void refresh_Success_ReadsCookieAndSetsNewCookies() throws Exception {
        RefreshRequestDTO refreshRequest = new RefreshRequestDTO();
        RefreshResponseDTO refreshResponse = new RefreshResponseDTO("Tokens refreshed successfully");

        RefreshTokenService.RefreshResult refreshResult = new RefreshTokenService.RefreshResult(
                refreshResponse,
                "new-access-token",
                "new-refresh-token"
        );

        when(refreshTokenService.refreshTokens(any())).thenReturn(refreshResult);

        Cookie refreshTokenCookie = new Cookie("refreshToken", "old-refresh-token");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);

        MvcResult result = mockMvc.perform(post("/api/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest))
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Tokens refreshed successfully"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        // Verify new cookies are set
        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).isNotNull();
        assertThat(cookies).hasSizeGreaterThanOrEqualTo(2);

        Cookie accessTokenCookie = findCookie(cookies, "accessToken");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isEqualTo("new-access-token");

        Cookie newRefreshTokenCookie = findCookie(cookies, "refreshToken");
        assertThat(newRefreshTokenCookie).isNotNull();
        assertThat(newRefreshTokenCookie.getValue()).isEqualTo("new-refresh-token");

        verify(refreshTokenService, times(1)).refreshTokens(any());
    }

    @Test
    void logout_Success_ClearsCookies() throws Exception {
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO();

        Cookie accessTokenCookie = new Cookie("accessToken", "valid-access-token");
        Cookie refreshTokenCookie = new Cookie("refreshToken", "valid-refresh-token");

        doNothing().when(logoutService).logout(any());

        MvcResult result = mockMvc.perform(post("/api/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest))
                        .cookie(accessTokenCookie, refreshTokenCookie))
                .andExpect(status().isNoContent())
                .andReturn();

        // Verify cookies are cleared (maxAge = 0)
        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).isNotNull();

        Cookie clearedAccessToken = findCookie(cookies, "accessToken");
        assertThat(clearedAccessToken).isNotNull();
        assertThat(clearedAccessToken.getMaxAge()).isEqualTo(0);

        Cookie clearedRefreshToken = findCookie(cookies, "refreshToken");
        assertThat(clearedRefreshToken).isNotNull();
        assertThat(clearedRefreshToken.getMaxAge()).isEqualTo(0);

        verify(logoutService, times(1)).logout(any());
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

    @Test
    void getCurrentUser_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());

        verify(accountService, never()).getAccountByEmail(anyString());
    }

    private Cookie findCookie(Cookie[] cookies, String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }
}
