package com.softserve.bookstoreapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.config.TestSecurityConfig;
import com.softserve.bookstoreapi.dto.ForgotPasswordDTO;
import com.softserve.bookstoreapi.dto.ResetPasswordDTO;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.exception.PasswordRecoveryTokenExpiredException;
import com.softserve.bookstoreapi.exception.TokenDeactivatedException;
import com.softserve.bookstoreapi.model.DeactivatedToken;
import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenDeserializer;
import com.softserve.bookstoreapi.security.TokenFactory;
import com.softserve.bookstoreapi.security.TokenSerializer;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PasswordRecoveryController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class PasswordRecoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private TokenFactory tokenFactory;

    @MockitoBean
    private TokenSerializer tokenSerializer;

    @MockitoBean
    private TokenDeserializer tokenDeserializer;

    @MockitoBean
    private DeactivatedTokenRepository deactivatedTokenRepository;

    @Test
    void forgotPassword_Success_EmailExists() throws Exception {
        ForgotPasswordDTO request = new ForgotPasswordDTO("test@example.com");
        Token token = new Token(UUID.randomUUID(), "test@example.com", List.of("PASSWORD_RECOVERY"), Instant.now(), Instant.now().plusSeconds(3600));

        when(accountService.existsByEmail(request.getEmail())).thenReturn(true);
        when(tokenFactory.createPasswordRecoveryToken(request.getEmail())).thenReturn(token);
        when(tokenSerializer.serialize(token)).thenReturn("valid.token.string");

        mockMvc.perform(post("/api/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("If the email exists, a password reset link has been sent"));

        verify(emailService).sendPasswordReset(eq("test@example.com"), eq("valid.token.string"));
    }

    @Test
    void forgotPassword_Success_EmailDoesNotExist() throws Exception {
        ForgotPasswordDTO request = new ForgotPasswordDTO("nonexistent@example.com");

        when(accountService.existsByEmail(request.getEmail())).thenReturn(false);

        mockMvc.perform(post("/api/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("If the email exists, a password reset link has been sent"));

        verify(emailService, never()).sendPasswordReset(anyString(), anyString());
    }

    @Test
    void forgotPassword_InvalidEmail() throws Exception {
        ForgotPasswordDTO request = new ForgotPasswordDTO("invalid-email");

        mockMvc.perform(post("/api/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_Success() throws Exception {
        ResetPasswordDTO request = new ResetPasswordDTO("valid.token.string", "newPassword123");
        Token token = new Token(UUID.randomUUID(), "test@example.com", List.of("PASSWORD_RECOVERY"), Instant.now(), Instant.now().plusSeconds(3600));

        when(tokenDeserializer.deserialize(request.getToken())).thenReturn(token);
        when(deactivatedTokenRepository.existsById(token.tokenId())).thenReturn(false);

        mockMvc.perform(post("/api/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password successfully changed"));

        verify(accountService).changePassword("test@example.com", "newPassword123");
        verify(deactivatedTokenRepository).save(any(DeactivatedToken.class));
    }

    @Test
    void resetPassword_InvalidToken_DeserializationFailed() throws Exception {
        ResetPasswordDTO request = new ResetPasswordDTO("invalid.token.string", "newPassword123");

        when(tokenDeserializer.deserialize(request.getToken())).thenThrow(new RuntimeException("Deserialization failed"));

        mockMvc.perform(post("/api/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resetPassword_TokenDeactivated() throws Exception {
        ResetPasswordDTO request = new ResetPasswordDTO("deactivated.token.string", "newPassword123");
        Token token = new Token(UUID.randomUUID(), "test@example.com", List.of("PASSWORD_RECOVERY"), Instant.now(), Instant.now().plusSeconds(3600));

        when(tokenDeserializer.deserialize(request.getToken())).thenReturn(token);
        when(deactivatedTokenRepository.existsById(token.tokenId())).thenReturn(true);

        mockMvc.perform(post("/api/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resetPassword_TokenExpired() throws Exception {
        ResetPasswordDTO request = new ResetPasswordDTO("expired.token.string", "newPassword123");
        Token token = new Token(UUID.randomUUID(), "test@example.com", List.of("PASSWORD_RECOVERY"), Instant.now(), Instant.now().minusSeconds(3600));

        when(tokenDeserializer.deserialize(request.getToken())).thenReturn(token);
        when(deactivatedTokenRepository.existsById(token.tokenId())).thenReturn(false);

        mockMvc.perform(post("/api/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resetPassword_InsufficientPermissions() throws Exception {
        ResetPasswordDTO request = new ResetPasswordDTO("wrong.permissions.token", "newPassword123");
        Token token = new Token(UUID.randomUUID(), "test@example.com", List.of("USER"), Instant.now(), Instant.now().plusSeconds(3600));

        when(tokenDeserializer.deserialize(request.getToken())).thenReturn(token);
        when(deactivatedTokenRepository.existsById(token.tokenId())).thenReturn(false);

        mockMvc.perform(post("/api/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void resetPassword_InvalidPassword() throws Exception {
        ResetPasswordDTO request = new ResetPasswordDTO("valid.token.string", "short");

        mockMvc.perform(post("/api/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

