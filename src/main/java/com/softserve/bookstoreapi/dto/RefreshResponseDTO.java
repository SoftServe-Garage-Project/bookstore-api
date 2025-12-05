package com.softserve.bookstoreapi.dto;

public record RefreshResponseDTO(
        String accessToken,
        String refreshToken
) {
}
