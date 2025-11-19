package com.softserve.bookstoreapi.dto;

import java.util.List;

public record LoginResponseDTO(
        String message,
        String email,
        List<String> roles
) {
}
