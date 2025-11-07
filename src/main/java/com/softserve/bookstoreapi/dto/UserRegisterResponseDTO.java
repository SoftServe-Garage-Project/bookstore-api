package com.softserve.bookstoreapi.dto;

import com.softserve.bookstoreapi.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterResponseDTO {

    private Long id;

    private String username;

    private String email;

    private UserRole role;

    private BigDecimal balance;
}
