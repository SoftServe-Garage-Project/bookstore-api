package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.UserRegisterRequestDTO;
import com.softserve.bookstoreapi.dto.UserRegisterResponseDTO;
import com.softserve.bookstoreapi.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/register")
public class RegistrationController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserRegisterResponseDTO> register(@Valid @RequestBody UserRegisterRequestDTO requestDTO) {
        UserRegisterResponseDTO registeredUser = userService.registerUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }
}
