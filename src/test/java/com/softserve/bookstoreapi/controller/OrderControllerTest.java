package com.softserve.bookstoreapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.dto.BuyNowRequestDTO;
import com.softserve.bookstoreapi.dto.CheckoutRequestDTO;
import com.softserve.bookstoreapi.dto.OrderDTO;
import com.softserve.bookstoreapi.dto.OrderItemDTO;
import com.softserve.bookstoreapi.model.enums.OrderStatus;
import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.service.impl.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private final String USER_EMAIL = "user@test.com";
    private final String FULL_NAME = "John Doe";
    private final String ADDRESS = "123 Main St";

    private Principal mockPrincipal;
    private OrderDTO mockOrderDTO;

    @BeforeEach
    void setUp() {
        mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(USER_EMAIL);

        OrderItemDTO itemDTO = new OrderItemDTO(
                1L, "Java Book", 1,
                new BigDecimal("100.00"), new BigDecimal("100.00"), BigDecimal.ZERO
        );

        mockOrderDTO = new OrderDTO(
                55L,
                new BigDecimal("100.00"),
                OrderStatus.PAID,
                PaymentMethod.BALANCE,
                LocalDateTime.now(),
                List.of(itemDTO),
                FULL_NAME,
                ADDRESS
        );
    }

    @Test
    @DisplayName("checkout: Success (No PromoCode) - Should return 200")
    void checkout_Success_NoPromo() throws Exception {
        CheckoutRequestDTO request = new CheckoutRequestDTO(null, FULL_NAME, ADDRESS);

        when(orderService.checkout(eq(USER_EMAIL), any(CheckoutRequestDTO.class))).thenReturn(mockOrderDTO);

        mockMvc.perform(post("/api/orders/checkout")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55L))
                .andExpect(jsonPath("$.fullName").value(FULL_NAME));

        verify(orderService).checkout(eq(USER_EMAIL), any(CheckoutRequestDTO.class));
    }

    @Test
    @DisplayName("checkout: Success (With PromoCode) - Should return 200")
    void checkout_Success_WithPromo() throws Exception {
        String promoCode = "SUMMER2026";
        CheckoutRequestDTO request = new CheckoutRequestDTO(promoCode, FULL_NAME, ADDRESS);

        when(orderService.checkout(eq(USER_EMAIL), any(CheckoutRequestDTO.class))).thenReturn(mockOrderDTO);

        mockMvc.perform(post("/api/orders/checkout")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55L));

        verify(orderService).checkout(eq(USER_EMAIL), any(CheckoutRequestDTO.class));
    }

    @Test
    @DisplayName("checkout: Validation Error - Missing Address")
    void checkout_MissingAddress() throws Exception {
        CheckoutRequestDTO invalidRequest = new CheckoutRequestDTO(null, FULL_NAME, "");

        mockMvc.perform(post("/api/orders/checkout")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).checkout(any(), any());
    }

    @Test
    @DisplayName("buyNow: Success - Should return 200")
    void buyNow_Success() throws Exception {
        BuyNowRequestDTO request = new BuyNowRequestDTO(1L, 1, null, FULL_NAME, ADDRESS);

        when(orderService.buyNow(any(BuyNowRequestDTO.class), eq(USER_EMAIL)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(post("/api/orders/buy-now")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55L))
                .andExpect(jsonPath("$.shippingAddress").value(ADDRESS));

        verify(orderService).buyNow(any(BuyNowRequestDTO.class), eq(USER_EMAIL));
    }

    @Test
    @DisplayName("buyNow: Validation Error - Quantity is 0")
    void buyNow_InvalidQuantity() throws Exception {
        BuyNowRequestDTO invalidRequest = new BuyNowRequestDTO(1L, 0, null, FULL_NAME, ADDRESS);

        mockMvc.perform(post("/api/orders/buy-now")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).buyNow(any(), any());
    }

    @Test
    @DisplayName("buyNow: Validation Error - Missing Name")
    void buyNow_MissingName() throws Exception {
        BuyNowRequestDTO invalidRequest = new BuyNowRequestDTO(1L, 1, null, "", ADDRESS);

        mockMvc.perform(post("/api/orders/buy-now")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).buyNow(any(), any());
    }

    @Test
    @DisplayName("buyNow: Validation Error - Book ID is null")
    void buyNow_NullBookId() throws Exception {
        BuyNowRequestDTO invalidRequest = new BuyNowRequestDTO(null, 1, "CODE", FULL_NAME, ADDRESS);

        mockMvc.perform(post("/api/orders/buy-now")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).buyNow(any(), any());
    }

    @Test
    @DisplayName("buyNow: Business Error (e.g. Insufficient Funds)")
    void buyNow_ServiceException() throws Exception {
        BuyNowRequestDTO request = new BuyNowRequestDTO(1L, 1, null, FULL_NAME, ADDRESS);

        when(orderService.buyNow(any(BuyNowRequestDTO.class), eq(USER_EMAIL)))
                .thenThrow(new RuntimeException("Insufficient funds"));

        mockMvc.perform(post("/api/orders/buy-now")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }
}