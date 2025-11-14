package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.model.RefreshToken;
import com.softserve.bookstoreapi.repository.RefreshTokenRepository;
import com.softserve.bookstoreapi.security.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void saveRefreshToken(Token token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }

        try {
            RefreshToken refreshToken = RefreshToken.builder()
                    .userEmail(token.subject())
                    .tokenId(token.id())
                    .createdAt(token.createdAt())
                    .expiresAt(token.expiresAt())
                    .used(false)
                    .revoked(false)
                    .build();

            refreshTokenRepository.save(refreshToken);
            log.debug("Successfully saved refresh token for user: {}", token.subject());
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to save refresh token (possible duplicate) for user: {}. Token ID: {}",
                    token.subject(), token.id());
            throw new IllegalStateException("Failed to store refresh token", e);
        }
    }
}
