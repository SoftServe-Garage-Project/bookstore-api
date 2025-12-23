package com.softserve.bookstoreapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.dto.TopUpDTO;
import com.softserve.bookstoreapi.dto.TransactionDTO;
import com.softserve.bookstoreapi.model.enums.TransactionStatus;
import com.softserve.bookstoreapi.model.enums.TransactionType;
import com.softserve.bookstoreapi.service.impl.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceController Tests")
class BalanceControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private BalanceController balanceController;

    private TopUpDTO validRequest;
    private TransactionDTO responseDto;
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(balanceController).build();
        validRequest = new TopUpDTO(
                new BigDecimal("100.00"),
                "Visa **** 1234",
                "My Wallet"
        );

        responseDto = new TransactionDTO(
                1L,
                null,
                55L,
                new BigDecimal("100.00"),
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED
        );
        mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user@test.com");
    }

    @Test
    @DisplayName("Should top up balance successfully")
    void topUpBalance_Success() throws Exception {
        when(balanceService.topUpBalance(any(TopUpDTO.class), any(Principal.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/topUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .principal(mockPrincipal)) // Важно: передаем Principal
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should return 400 for invalid amount")
    void topUpBalance_InvalidAmount_Returns400() throws Exception {
        TopUpDTO invalidRequest = new TopUpDTO(
                new BigDecimal("-50.00"),
                "Sender",
                "Recipient"
        );

        mockMvc.perform(post("/api/topUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for empty body")
    void topUpBalance_NullBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/topUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest());
    }
}