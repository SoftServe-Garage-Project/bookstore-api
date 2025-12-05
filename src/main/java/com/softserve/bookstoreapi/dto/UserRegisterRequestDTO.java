package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDTO(
        @NotBlank(message = "{validation.user.username.notblank}")
        @Size(min = 3, max = 100, message = "{validation.user.username.size}")
        String username,

        @NotBlank(message = "{validation.user.email.notblank}")
        @Email(message = "{validation.user.email.invalid}")
        @Size(max = 150, message = "{validation.user.email.size}")
        String email,

        @NotBlank(message = "{validation.user.password.notblank}")
        @Size(min = 8, max = 255, message = "{validation.user.password.size}")
        String password
) {
}
