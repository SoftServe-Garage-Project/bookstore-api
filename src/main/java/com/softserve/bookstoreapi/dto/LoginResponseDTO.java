package com.softserve.bookstoreapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
    private String message;
    private String email;
    private List<String> roles;

    public LoginResponseDTO(String email, List<String> roles) {
        this.message = "Login successful";
        this.email = email;
        this.roles = roles;
    }
}
