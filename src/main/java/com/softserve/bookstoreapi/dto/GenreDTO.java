package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.NotBlank;

public record GenreDTO(
        Long id,

        @NotBlank(message = "{validation.genre.name.notblank}")
        String name,

        String description
) {}