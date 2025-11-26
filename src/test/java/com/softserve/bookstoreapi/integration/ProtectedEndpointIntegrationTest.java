package com.softserve.bookstoreapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.config.TestContainersConfig;
import com.softserve.bookstoreapi.dto.LoginRequestDTO;
import com.softserve.bookstoreapi.dto.LoginResponseDTO;
import com.softserve.bookstoreapi.dto.LogoutRequestDTO;
import com.softserve.bookstoreapi.dto.UserRegisterRequestDTO;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Protected Endpoint Integration Tests")
class ProtectedEndpointIntegrationTest {

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

    private String validAccessToken;
    private String validRefreshToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        deactivatedTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        accountRepository.deleteAll();

        // Register and login to get valid tokens
        UserRegisterRequestDTO registerRequest = new UserRegisterRequestDTO(
                "testuser",
                "test@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginRequest = new LoginRequestDTO(
                "test@example.com",
                "password123"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponseDTO loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                LoginResponseDTO.class
        );

        validAccessToken = loginResponse.accessToken();
        validRefreshToken = loginResponse.refreshToken();
    }

    @Test
    @DisplayName("Should access protected endpoint with valid token")
    void accessProtectedEndpoint_WithValidToken_Returns200() throws Exception {
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny access without token")
    void accessProtectedEndpoint_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/main"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with invalid token")
    void accessProtectedEndpoint_WithInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with malformed Bearer header")
    void accessProtectedEndpoint_WithMalformedHeader_Returns401() throws Exception {
        // Missing "Bearer " prefix
        mockMvc.perform(get("/main")
                        .header("Authorization", validAccessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with empty Bearer token")
    void accessProtectedEndpoint_WithEmptyToken_Returns401() throws Exception {
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with deactivated token")
    void accessProtectedEndpoint_WithDeactivatedToken_Returns401() throws Exception {
        // First, verify token works
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk());

        // Logout to deactivate the token
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO(validAccessToken, validRefreshToken);
        mockMvc.perform(post("/api/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        assertThat(deactivatedTokenRepository.count()).isEqualTo(1);

        // Try to access with deactivated token
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with Basic auth instead of Bearer")
    void accessProtectedEndpoint_WithBasicAuth_Returns401() throws Exception {
        mockMvc.perform(get("/main")
                        .header("Authorization", "Basic dGVzdDp0ZXN0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle multiple requests with same valid token")
    void accessProtectedEndpoint_MultipleRequests_AllSucceed() throws Exception {
        // Make multiple requests with the same token
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/main")
                            .header("Authorization", "Bearer " + validAccessToken))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Should access public endpoints without token")
    void accessPublicEndpoint_WithoutToken_Returns200Or201() throws Exception {
        // Registration endpoint should be public
        UserRegisterRequestDTO newUser = new UserRegisterRequestDTO(
                "newuser",
                "newuser@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated());

        // Login endpoint should be public
        LoginRequestDTO loginRequest = new LoginRequestDTO(
                "newuser@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should maintain separate sessions for different users")
    void multipleConcurrentUsers_IndependentSessions() throws Exception {
        // Register second user
        UserRegisterRequestDTO secondUser = new UserRegisterRequestDTO(
                "seconduser",
                "second@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isCreated());

        // Login second user
        LoginRequestDTO secondLogin = new LoginRequestDTO(
                "second@example.com",
                "password123"
        );

        MvcResult secondLoginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondLogin)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponseDTO secondLoginResponse = objectMapper.readValue(
                secondLoginResult.getResponse().getContentAsString(),
                LoginResponseDTO.class
        );

        String secondAccessToken = secondLoginResponse.accessToken();

        // Both users should be able to access protected endpoint
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + secondAccessToken))
                .andExpect(status().isOk());

        // Logout first user shouldn't affect second user
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO(validAccessToken, validRefreshToken);
        mockMvc.perform(post("/api/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        // First user's token should be invalid
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isUnauthorized());

        // Second user's token should still work
        mockMvc.perform(get("/main")
                        .header("Authorization", "Bearer " + secondAccessToken))
                .andExpect(status().isOk());
    }
}

