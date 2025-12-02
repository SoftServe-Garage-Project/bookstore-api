package com.softserve.bookstoreapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.config.TestSecurityConfig;
import com.softserve.bookstoreapi.dto.UserRegisterRequestDTO;
import com.softserve.bookstoreapi.dto.UserRegisterResponseDTO;
import com.softserve.bookstoreapi.exception.EmailAlreadyExistsException;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.service.impl.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@Import(TestSecurityConfig.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
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
    void register_ValidRequest_Returns201() throws Exception {
        when(accountService.registerUser(any(UserRegisterRequestDTO.class))).thenReturn(validResponse);

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
    void register_InvalidEmail_Returns400() throws Exception {
        UserRegisterRequestDTO invalidRequest = new UserRegisterRequestDTO(
                "testuser",
                "invalid-email",
                "password123"
        );

        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void register_EmailExists_Returns409() throws Exception {
        when(accountService.registerUser(any(UserRegisterRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistsException("error.email.already.exists", "test@example.com"));

        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("error.email.already.exists"));
    }

    @Test
    @WithMockUser
    void register_MissingUsername_Returns400() throws Exception {
        UserRegisterRequestDTO invalidRequest = new UserRegisterRequestDTO(
                "",
                "test@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void register_ShortPassword_Returns400() throws Exception {
        UserRegisterRequestDTO invalidRequest = new UserRegisterRequestDTO(
                "testuser",
                "test@example.com",
                "pass"
        );

        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void register_MissingEmail_Returns400() throws Exception {
        UserRegisterRequestDTO invalidRequest = new UserRegisterRequestDTO(
                "testuser",
                "",
                "password123"
        );

        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}

