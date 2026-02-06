package com.softserve.bookstoreapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.bookstoreapi.dto.ReviewRequestDTO;
import com.softserve.bookstoreapi.dto.ReviewResponseDTO;
import com.softserve.bookstoreapi.service.impl.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ReviewService reviewService;

    private final String EMAIL = "user@test.com";

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("addReview: Success (201 Created)")
    void addReview_Success() throws Exception {
        ReviewRequestDTO request = new ReviewRequestDTO(1L, 5, "Good!");
        ReviewResponseDTO response = new ReviewResponseDTO(100L, 1L, "User", 5, "Good!", LocalDateTime.now());

        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(EMAIL);

        when(reviewService.addReview(eq(EMAIL), any(ReviewRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    @DisplayName("addReview: Fail - Rating too high (Validation)")
    void addReview_InvalidRatingHigh() throws Exception {
        ReviewRequestDTO invalidRequest = new ReviewRequestDTO(1L, 6, "Good!");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Очікуємо 400

        verify(reviewService, never()).addReview(any(), any());
    }

    @Test
    @DisplayName("addReview: Fail - Rating too low (Validation)")
    void addReview_InvalidRatingLow() throws Exception {
        ReviewRequestDTO invalidRequest = new ReviewRequestDTO(1L, 0, "Bad!");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("addReview: Fail - Empty Comment (Validation)")
    void addReview_EmptyComment() throws Exception {
        ReviewRequestDTO invalidRequest = new ReviewRequestDTO(1L, 5, "");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("deleteReview: Should return 204 No Content")
    void deleteReview_Success() throws Exception {
        Long reviewId = 1L;

        doNothing().when(reviewService).deleteReview(eq(reviewId), eq(EMAIL));

        mockMvc.perform(delete("/api/reviews/{id}", reviewId)
                        .principal(() -> EMAIL))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReview(reviewId, EMAIL);
    }
}