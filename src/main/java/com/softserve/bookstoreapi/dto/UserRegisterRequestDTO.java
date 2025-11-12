package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterRequestDTO {

    @NotBlank(message = "{validation.user.username.notblank}")
    @Size(min = 3, max = 100, message = "{validation.user.username.size}")
    private String username;

    @NotBlank(message = "{validation.user.email.notblank}")
    @Email(message = "{validation.user.email.invalid}")
    @Size(max = 150, message = "{validation.user.email.size}")
    private String email;

    @NotBlank(message = "{validation.user.password.notblank}")
    @Size(min = 8, max = 255, message = "{validation.user.password.size}")
    private String password;
}
