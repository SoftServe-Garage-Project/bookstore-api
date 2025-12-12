package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.ForgotPasswordDTO;
import com.softserve.bookstoreapi.dto.ResetPasswordDTO;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.exception.InsufficientPermissionsException;
import com.softserve.bookstoreapi.exception.PasswordRecoveryTokenExpiredException;
import com.softserve.bookstoreapi.exception.TokenDeactivatedException;
import com.softserve.bookstoreapi.model.DeactivatedToken;
import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenDeserializer;
import com.softserve.bookstoreapi.security.TokenFactory;
import com.softserve.bookstoreapi.security.TokenSerializer;
import com.softserve.bookstoreapi.service.impl.AccountService;
import com.softserve.bookstoreapi.service.impl.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import static com.softserve.bookstoreapi.logger.LoggerUtils.obfuscate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PasswordRecoveryController {
    private final AccountService accountService;
    private final EmailService emailService;
    private final TokenFactory tokenFactory;
    private final TokenSerializer tokenSerializer;
    private final TokenDeserializer tokenDeserializer;
    private final DeactivatedTokenRepository deactivatedTokenRepository;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordDTO request) {
        String email = request.getEmail();

        if (accountService.existsByEmail(email)) {
            var token = tokenFactory.createPasswordRecoveryToken(email);
            emailService.sendPasswordReset(email, tokenSerializer.serialize(token));
            log.info("Password recovery email sent to: {}", obfuscate(email));
        } else {
            log.info("Password recovery attempt for non-existent email: {}", obfuscate(email));
        }

        // Always return success to prevent email enumeration
        return ResponseEntity.ok("If the email exists, a password reset link has been sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPasswordPost(@RequestBody @Valid ResetPasswordDTO resetPasswordDTO) {
        String newPassword = resetPasswordDTO.getNewPassword();
        Token validToken = validateToken(resetPasswordDTO.getToken());

        accountService.changePassword(validToken.subject(), newPassword);
        deactivatedTokenRepository.save(
                new DeactivatedToken(validToken.tokenId(), Instant.now(), validToken.expiresAt())
        );

        log.info("Password successfully changed for user: {}", obfuscate(validToken.subject()));

        return ResponseEntity.ok("Password successfully changed");
    }

    private Token validateToken(String tokenString) {
        if (tokenString == null || tokenString.isEmpty()) {throw new InvalidJwtToken("token.invalid");}

        Token token;
        try {token = tokenDeserializer.deserialize(tokenString);} catch (Exception e) {
            throw new InvalidJwtToken("token.invalid");
        }

        if (deactivatedTokenRepository.existsById(token.tokenId())) {throw new TokenDeactivatedException("token.deactivated", token.tokenId());}
        if (token.expiresAt().isBefore(Instant.now())) {throw new PasswordRecoveryTokenExpiredException("token.expired");}
        if (!token.authorities().contains("PASSWORD_RECOVERY")) {throw new InsufficientPermissionsException("permission.denied");}

        return token;
    }
}