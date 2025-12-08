package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDTO(
        @NotBlank(message = "Access token is required")
        String accessToken,
        
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
