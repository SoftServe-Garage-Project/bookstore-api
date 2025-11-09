package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.UserRegisterRequestDTO;
import com.softserve.bookstoreapi.dto.UserRegisterResponseDTO;
import com.softserve.bookstoreapi.service.impl.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/register")
public class RegistrationController {
    private final AccountService accountService;

    public ResponseEntity<UserRegisterResponseDTO> register(@Valid @RequestBody UserRegisterRequestDTO requestDTO) {
        UserRegisterResponseDTO registeredUser = accountService.registerUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }
}
