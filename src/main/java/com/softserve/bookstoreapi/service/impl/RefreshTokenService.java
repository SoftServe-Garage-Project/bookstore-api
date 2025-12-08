package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.RefreshRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshResponseDTO;
import com.softserve.bookstoreapi.exception.AccountNotFoundException;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.exception.RefreshTokenExpiredException;
import com.softserve.bookstoreapi.exception.RefreshTokenInvalidException;
import com.softserve.bookstoreapi.exception.RefreshTokenStorageException;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.RefreshToken;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.repository.RefreshTokenRepository;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenCookieJweStringDeserializer;
import com.softserve.bookstoreapi.security.TokenFactory;
import com.softserve.bookstoreapi.security.TokenSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.softserve.bookstoreapi.logger.LoggerUtils.obfuscate;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountRepository accountRepository;
    private final TokenCookieJweStringDeserializer tokenDeserializer;
    private final TokenFactory tokenFactory;
    private final TokenSerializer tokenSerializer;

    @Transactional
    public void saveRefreshToken(Token token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }

        try {
            RefreshToken refreshToken = RefreshToken.builder()
                    .userEmail(token.subject())
                    .tokenId(token.tokenId())
                    .createdAt(token.createdAt())
                    .expiresAt(token.expiresAt())
                    .used(false)
                    .revoked(false)
                    .build();

            refreshTokenRepository.save(refreshToken);
            log.trace("Successfully saved refresh token for user: {}", token.subject());
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to save refresh token (possible duplicate) for user: {}. Token ID: {}",
                    token.subject(), token.tokenId());
            throw new RefreshTokenStorageException("Failed to store refresh token", e);
        } catch (Exception e) {
            log.error("Unexpected error while saving refresh token for user: {}. Token ID: {}",
                    token.subject(), token.tokenId(), e);
            throw new RefreshTokenStorageException("Failed to store refresh token due to unexpected error", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenId(UUID tokenId) {
        return refreshTokenRepository.findById(tokenId);
    }

    @Transactional
    public void markAsUsed(UUID tokenId) {
        refreshTokenRepository.findById(tokenId).ifPresent(token -> {
            token.setUsed(true);
            refreshTokenRepository.save(token);
            log.trace("Marked refresh token as used. Token ID: {}", tokenId);
        });
    }


    @Transactional
    public RefreshResponseDTO refreshTokens(RefreshRequestDTO request) {
        log.debug("Refresh token request received");

        Token refreshToken = deserializeAndValidateToken(request.refreshToken());
        Account account = getAccountForToken(refreshToken);

        return generateNewTokens(account);
    }

    private Token deserializeAndValidateToken(String tokenString) {
        Token token;
        try {
            token = tokenDeserializer.deserialize(tokenString);
        } catch (Exception e) {
            log.error("Failed to deserialize refresh token: {}", e.getMessage());
            throw new InvalidJwtToken("Invalid refresh token format");
        }

        if (token == null) {
            throw new InvalidJwtToken("Refresh token deserialization returned null");
        }

        log.debug("Processing refresh token for user: {}", obfuscate(token.subject()));

        RefreshToken storedToken = findByTokenId(token.tokenId())
                .orElseThrow(() -> new InvalidJwtToken("Refresh token not found in database. Token ID: " + token.tokenId()));

        validateRefreshToken(storedToken);
        markAsUsed(token.tokenId());

        return token;
    }

    private void validateRefreshToken(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException("Refresh token has expired", token.getTokenId());
        }

        if (token.isUsed()) {
            throw new RefreshTokenInvalidException("Refresh token already used", token.getTokenId(), "used");
        }

        if (token.isRevoked()) {
            throw new RefreshTokenInvalidException("Refresh token has been revoked", token.getTokenId(), "revoked");
        }
    }

    private Account getAccountForToken(Token token) {
        return accountRepository.findByEmail(token.subject())
                .orElseThrow(() -> new AccountNotFoundException("User not found: " + obfuscate(token.subject())));
    }

    private RefreshResponseDTO generateNewTokens(Account account) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                account.getEmail(),
                null,
                account.getAuthorities()
        );

        Token newAccessToken = tokenFactory.createAccessToken(authentication);
        String accessTokenString = tokenSerializer.serialize(newAccessToken);

        Token newRefreshToken = tokenFactory.createRefreshToken(authentication);
        String refreshTokenString = tokenSerializer.serialize(newRefreshToken);
        saveRefreshToken(newRefreshToken);

        log.info("Successfully refreshed tokens for user: {}", obfuscate(account.getEmail()));

        return new RefreshResponseDTO(accessTokenString, refreshTokenString);
    }

    @Transactional
    public void revokeRefreshToken(UUID tokenId) {
        refreshTokenRepository.findById(tokenId).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.trace("Revoked refresh token. Token ID: {}", tokenId);
        });
    }

    @Transactional
    public void revokeRefreshTokenByString(String refreshTokenString) {
        try {
            Token refreshToken = tokenDeserializer.deserialize(refreshTokenString);
            if (refreshToken != null) {
                revokeRefreshToken(refreshToken.tokenId());
                log.info("Successfully revoked refresh token for user: {}", obfuscate(refreshToken.subject()));
            }
        } catch (Exception e) {
            log.error("Failed to deserialize refresh token for revocation: {}", e.getMessage());
            throw new InvalidJwtToken("Invalid refresh token format");
        }
    }
}