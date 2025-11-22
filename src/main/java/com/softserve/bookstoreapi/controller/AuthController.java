package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.LoginRequestDTO;
import com.softserve.bookstoreapi.dto.LoginResponseDTO;
import com.softserve.bookstoreapi.dto.LogoutRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshResponseDTO;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.LogoutService;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;
    private final RefreshTokenService refreshTokenService;
    private final LogoutService logoutService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {

        LoginResponseDTO responseDTO = accountService.login(loginRequest);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDTO> refresh(
            @Valid @RequestBody RefreshRequestDTO refreshRequest) {

        RefreshResponseDTO responseDTO = refreshTokenService.refreshTokens(refreshRequest);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequestDTO logoutRequest) {

        logoutService.logout(logoutRequest);
        return ResponseEntity.noContent().build();
    }
}
