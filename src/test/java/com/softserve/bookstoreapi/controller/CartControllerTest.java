package com.softserve.bookstoreapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.dto.CartDTO;
import com.softserve.bookstoreapi.dto.CartItemRequestDTO;
import com.softserve.bookstoreapi.dto.CartItemResponseDTO;
import com.softserve.bookstoreapi.service.impl.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    private final String USER_EMAIL = "user@test.com";

    @Test
    @DisplayName("POST /api/cart/items - Success: Should add item to cart")

    void addToCart_Success() throws Exception {
        CartItemRequestDTO requestDto = new CartItemRequestDTO(1L, 2);

        CartItemResponseDTO responseDto = new CartItemResponseDTO(
                100L, 1L, "Harry Potter", 2, new BigDecimal("20.00")
        );
        when(cartService.addItemToCart(eq(USER_EMAIL), any(CartItemRequestDTO.class)))
                .thenReturn(responseDto);
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(USER_EMAIL);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.bookTitle").value("Harry Potter"))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(cartService, times(1)).addItemToCart(eq(USER_EMAIL), any(CartItemRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/cart/items - Validation Error: Should return 400 when quantity is invalid")
    void addToCart_ValidationError() throws Exception {
        CartItemRequestDTO invalidRequest = new CartItemRequestDTO(1L, 0);

        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(USER_EMAIL);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyString(), any());
    }

    @Test
    @DisplayName("DELETE /api/cart/items/{itemId} - Success: Should remove item")
    void removeFromCart_Success() throws Exception {
        Long cartItemId = 55L;
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(USER_EMAIL);
        doNothing().when(cartService).removeCartItem(cartItemId, USER_EMAIL);

        mockMvc.perform(delete("/api/cart/items/{itemId}", cartItemId)
                        .principal(mockPrincipal))
                .andExpect(status().isNoContent());

        verify(cartService).removeCartItem(cartItemId, USER_EMAIL);
    }

    @Test
    @DisplayName("DELETE /api/cart/items/{itemId} - Error: Should return error status if service throws exception")
    void removeFromCart_ServiceException() throws Exception {
        Long cartItemId = 99L;
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(USER_EMAIL);

        doThrow(new RuntimeException("Access denied"))
                .when(cartService).removeCartItem(cartItemId, USER_EMAIL);
        mockMvc.perform(delete("/api/cart/items/{itemId}", cartItemId)
                        .principal(mockPrincipal))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("GET /api/cart - Success: Should return user cart")
    void getCart_Success() throws Exception {
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(USER_EMAIL);

        CartDTO cartDTO = new CartDTO(
                10L,
                List.of(new CartItemResponseDTO(1L, 2L, "Book", 1, BigDecimal.TEN)),
                BigDecimal.TEN
        );

        when(cartService.getUserCart(USER_EMAIL)).thenReturn(cartDTO);
        mockMvc.perform(get("/api/cart")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.totalPrice").value(10))
                .andExpect(jsonPath("$.items[0].bookTitle").value("Book"));

        verify(cartService).getUserCart(USER_EMAIL);
    }
}