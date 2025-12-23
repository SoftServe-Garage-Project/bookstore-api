package com.softserve.bookstoreapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordDTO {
    @NotBlank(message = "{validation.token.notblank}")
    private String token;

    @NotBlank(message = "{validation.user.password.notblank}")
    @Size(min = 8, max = 255, message = "{validation.user.password.size}")
    private String newPassword;
}
