package com.softserve.bookstoreapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.config.TestSecurityConfig;
import com.softserve.bookstoreapi.dto.UserRegisterRequestDTO;
import com.softserve.bookstoreapi.dto.UserRegisterResponseDTO;
import com.softserve.bookstoreapi.exception.EmailAlreadyExistsException;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.service.impl.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@Import(TestSecurityConfig.class)
@DisplayName("RegistrationController Tests")
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private UserRegisterRequestDTO validRequest;
    private UserRegisterResponseDTO validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new UserRegisterRequestDTO(
                "testuser",
                "test@example.com",
                "password123"
        );

        validResponse = new UserRegisterResponseDTO(
                1L,
                "testuser",
                "test@example.com",
                UserRole.ROLE_CUSTOMER,
                BigDecimal.ZERO
        );
    }

    @Test
    @WithMockUser
    @DisplayName("Should register user successfully")
    void register_ValidRequest_Returns201() throws Exception {
        // Given
        when(accountService.registerUser(any(UserRegisterRequestDTO.class))).thenReturn(validResponse);

        // When & Then
        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 for invalid email")
    void register_InvalidEmail_Returns400() throws Exception {
        // Given
        UserRegisterRequestDTO invalidRequest = new UserRegisterRequestDTO(
                "testuser",
                "invalid-email",
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 409 when email already exists")
    void register_EmailExists_Returns409() throws Exception {
        // Given
        when(accountService.registerUser(any(UserRegisterRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistsException("error.email.already.exists", "test@example.com"));

        // When & Then
        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("error.email.already.exists"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 for missing username")
    void register_MissingUsername_Returns400() throws Exception {
        // Given
        UserRegisterRequestDTO invalidRequest = new UserRegisterRequestDTO(
                "",
                "test@example.com",
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 for short password")
    void register_ShortPassword_Returns400() throws Exception {
        // Given
        UserRegisterRequestDTO invalidRequest = new UserRegisterRequestDTO(
                "testuser",
                "test@example.com",
                "pass"
        );

        // When & Then
        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 for missing email")
    void register_MissingEmail_Returns400() throws Exception {
        // Given
        UserRegisterRequestDTO invalidRequest = new UserRegisterRequestDTO(
                "testuser",
                "",
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}

