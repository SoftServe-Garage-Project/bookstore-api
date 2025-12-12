package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.model.DeactivatedToken;
import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.softserve.bookstoreapi.logger.LoggerUtils.obfuscate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenDeactivationService {

    private final DeactivatedTokenRepository deactivatedTokenRepository;
    private final TokenDeserializer tokenDeserializer;

    @Transactional
    public void deactivateAccessToken(String accessTokenString) {
        try {
            Token accessToken = tokenDeserializer.deserialize(accessTokenString);

            if (accessToken == null) {
                throw new InvalidJwtToken("Access token deserialization returned null");
            }

            DeactivatedToken deactivatedToken = DeactivatedToken.builder()
                    .id(accessToken.tokenId())
                    .keepUntil(accessToken.expiresAt())
                    .build();

            deactivatedTokenRepository.save(deactivatedToken);
            log.info("Successfully deactivated access token for user: {}", obfuscate(accessToken.subject()));

        } catch (Exception e) {
            log.error("Failed to deactivate access token: {}", e.getMessage());
            throw new InvalidJwtToken("Invalid access token format");
        }
    }
}

