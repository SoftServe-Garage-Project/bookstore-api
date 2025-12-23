package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorDTO(
        @NotBlank(message = "{validation.author.firstname.notblank}")
        @Size(min = 2, max = 100, message = "{validation.author.firstname.size}")
        String firstName,

        @NotBlank(message = "{validation.author.lastname.notblank}")
        @Size(min = 2, max = 100, message = "{validation.author.lastname.size}")
        String lastName
) {}
