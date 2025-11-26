package com.softserve.bookstoreapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.config.TestContainersConfig;
import com.softserve.bookstoreapi.dto.*;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Authentication Flow Integration Tests")
class AuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private DeactivatedTokenRepository deactivatedTokenRepository;

    private UserRegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        deactivatedTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        accountRepository.deleteAll();

        registerRequest = new UserRegisterRequestDTO(
                "integrationuser",
                "integration@test.com",
                "password123"
        );
    }

    @Test
    @DisplayName("Full authentication flow: Register -> Login -> Refresh -> Logout")
    void fullAuthFlow_RegisterLoginRefreshLogout_Success() throws Exception {
        // STEP 1: Register new user
        MvcResult registerResult = mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"))
                .andReturn();

        UserRegisterResponseDTO registerResponse = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(),
                UserRegisterResponseDTO.class
        );

        assertThat(registerResponse.id()).isNotNull();
        assertThat(accountRepository.findByEmail("integration@test.com")).isPresent();

        // STEP 2: Login with registered credentials
        LoginRequestDTO loginRequest = new LoginRequestDTO(
                "integration@test.com",
                "password123"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        LoginResponseDTO loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                LoginResponseDTO.class
        );

        String accessToken = loginResponse.accessToken();
        String refreshToken = loginResponse.refreshToken();

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();
        assertThat(refreshTokenRepository.count()).isEqualTo(1);

        // STEP 3: Access protected endpoint with access token
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // STEP 4: Refresh tokens
        RefreshRequestDTO refreshRequest = new RefreshRequestDTO(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        RefreshResponseDTO refreshResponse = objectMapper.readValue(
                refreshResult.getResponse().getContentAsString(),
                RefreshResponseDTO.class
        );

        String newAccessToken = refreshResponse.accessToken();
        String newRefreshToken = refreshResponse.refreshToken();

        assertThat(newAccessToken).isNotBlank();
        assertThat(newRefreshToken).isNotBlank();
        assertThat(newAccessToken).isNotEqualTo(accessToken);
        assertThat(refreshTokenRepository.count()).isEqualTo(2); // Old + new refresh token

        // STEP 5: Access protected endpoint with new access token
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());

        // STEP 6: Logout
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO(newAccessToken, newRefreshToken);

        mockMvc.perform(post("/api/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        assertThat(deactivatedTokenRepository.count()).isEqualTo(1); // Access token deactivated

        // STEP 7: Try to access protected endpoint with logged-out token (should fail)
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should register and login successfully")
    void registerAndLogin_NewUser_Success() throws Exception {
        // Register
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequestDTO loginRequest = new LoginRequestDTO(
                "integration@test.com",
                "password123"
        );

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("Should return 401 for invalid credentials")
    void loginWithInvalidCredentials_Returns401() throws Exception {
        // Register user first
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Try to login with wrong password
        LoginRequestDTO invalidLogin = new LoginRequestDTO(
                "integration@test.com",
                "wrongpassword"
        );

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("error.auth.invalid.credentials"));
    }

    @Test
    @DisplayName("Should return 409 when registering with existing email")
    void registerWithExistingEmail_Returns409() throws Exception {
        // Register user first time
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Try to register again with same email
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("error.email.already.exists"));
    }

    @Test
    @DisplayName("Should handle concurrent logins for same user")
    void concurrentLogins_SameUser_Success() throws Exception {
        // Register
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginRequest = new LoginRequestDTO(
                "integration@test.com",
                "password123"
        );

        // First login
        MvcResult firstLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Second login (simulating login from another device)
        MvcResult secondLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponseDTO firstResponse = objectMapper.readValue(
                firstLogin.getResponse().getContentAsString(),
                LoginResponseDTO.class
        );

        LoginResponseDTO secondResponse = objectMapper.readValue(
                secondLogin.getResponse().getContentAsString(),
                LoginResponseDTO.class
        );

        // Both tokens should work
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + firstResponse.accessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + secondResponse.accessToken()))
                .andExpect(status().isOk());

        assertThat(refreshTokenRepository.count()).isEqualTo(2); // Two refresh tokens
    }
}

