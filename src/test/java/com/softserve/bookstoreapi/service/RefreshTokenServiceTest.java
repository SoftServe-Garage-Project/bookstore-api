package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.RefreshRequestDTO;
import com.softserve.bookstoreapi.dto.RefreshResponseDTO;
import com.softserve.bookstoreapi.exception.AccountNotFoundException;
import com.softserve.bookstoreapi.exception.InvalidJwtToken;
import com.softserve.bookstoreapi.exception.RefreshTokenExpiredException;
import com.softserve.bookstoreapi.exception.RefreshTokenInvalidException;
import com.softserve.bookstoreapi.exception.RefreshTokenStorageException;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.RefreshToken;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.repository.RefreshTokenRepository;
import com.softserve.bookstoreapi.security.Token;
import com.softserve.bookstoreapi.security.TokenCookieJweStringDeserializer;
import com.softserve.bookstoreapi.security.TokenFactory;
import com.softserve.bookstoreapi.security.TokenSerializer;
import com.softserve.bookstoreapi.service.impl.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TokenCookieJweStringDeserializer tokenDeserializer;

    @Mock
    private TokenFactory tokenFactory;

    @Mock
    private TokenSerializer tokenSerializer;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private Token validToken;
    private RefreshToken storedRefreshToken;
    private Account testAccount;
    private UUID tokenId;

    @BeforeEach
    void setUp() {
        tokenId = UUID.randomUUID();

        validToken = new Token(
                tokenId,
                "test@example.com",
                List.of("ROLE_CUSTOMER"),
                Instant.now(),
                Instant.now().plusSeconds(604800)
        );

        storedRefreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .userEmail("test@example.com")
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(604800))
                .used(false)
                .revoked(false)
                .build();

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUsername("testuser");
        testAccount.setEmail("test@example.com");
        testAccount.setPassword("encodedPassword");
        testAccount.setRole(UserRole.ROLE_CUSTOMER);
        testAccount.setBalance(BigDecimal.ZERO);
    }

    @Test
    void saveRefreshToken_Success() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(storedRefreshToken);

        refreshTokenService.saveRefreshToken(validToken);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void saveRefreshToken_DuplicateToken_ThrowsException() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThatThrownBy(() -> refreshTokenService.saveRefreshToken(validToken))
                .isInstanceOf(RefreshTokenStorageException.class)
                .hasMessageContaining("Failed to store refresh token");

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void saveRefreshToken_NullToken_ThrowsException() {
        assertThatThrownBy(() -> refreshTokenService.saveRefreshToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token cannot be null");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void refreshTokens_ValidToken_ReturnsNewTokens() {
        RefreshRequestDTO request = new RefreshRequestDTO("validRefreshToken");
        Token newAccessToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_CUSTOMER"),
                Instant.now(),
                Instant.now().plusSeconds(900)
        );
        Token newRefreshToken = new Token(
                UUID.randomUUID(),
                "test@example.com",
                List.of("ROLE_CUSTOMER"),
                Instant.now(),
                Instant.now().plusSeconds(604800)
        );

        when(tokenDeserializer.deserialize("validRefreshToken")).thenReturn(validToken);
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(storedRefreshToken));
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));
        when(tokenFactory.createAccessToken(any(Authentication.class))).thenReturn(newAccessToken);
        when(tokenFactory.createRefreshToken(any(Authentication.class))).thenReturn(newRefreshToken);
        when(tokenSerializer.serialize(newAccessToken)).thenReturn("newAccessToken");
        when(tokenSerializer.serialize(newRefreshToken)).thenReturn("newRefreshToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(storedRefreshToken);

        RefreshResponseDTO result = refreshTokenService.refreshTokens(request);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("newAccessToken");
        assertThat(result.refreshToken()).isEqualTo("newRefreshToken");

        verify(tokenDeserializer).deserialize("validRefreshToken");
        verify(refreshTokenRepository, times(2)).findById(tokenId); // Called twice: findByTokenId() and markAsUsed()
        verify(accountRepository).findByEmail("test@example.com");
        verify(tokenFactory).createAccessToken(any(Authentication.class));
        verify(tokenFactory).createRefreshToken(any(Authentication.class));
    }

    @Test
    void refreshTokens_InvalidToken_ThrowsException() {
        RefreshRequestDTO request = new RefreshRequestDTO("invalidToken");
        when(tokenDeserializer.deserialize("invalidToken")).thenThrow(new RuntimeException("Malformed JWT"));

        assertThatThrownBy(() -> refreshTokenService.refreshTokens(request))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessageContaining("Invalid refresh token format");

        verify(tokenDeserializer).deserialize("invalidToken");
        verify(refreshTokenRepository, never()).findById(any());
    }

    @Test
    void refreshTokens_ExpiredToken_ThrowsException() {
        RefreshRequestDTO request = new RefreshRequestDTO("expiredToken");
        RefreshToken expiredToken = RefreshToken.builder()
                .tokenId(tokenId)
                .userEmail("test@example.com")
                .createdAt(Instant.now().minusSeconds(700000))
                .expiresAt(Instant.now().minusSeconds(1000))
                .used(false)
                .revoked(false)
                .build();

        when(tokenDeserializer.deserialize("expiredToken")).thenReturn(validToken);
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.refreshTokens(request))
                .isInstanceOf(RefreshTokenExpiredException.class)
                .hasMessageContaining("Refresh token has expired");

        verify(tokenDeserializer).deserialize("expiredToken");
        verify(refreshTokenRepository).findById(tokenId);
        verify(accountRepository, never()).findByEmail(any());
    }

    @Test
    void refreshTokens_UsedToken_ThrowsException() {
        RefreshRequestDTO request = new RefreshRequestDTO("usedToken");
        storedRefreshToken.setUsed(true);

        when(tokenDeserializer.deserialize("usedToken")).thenReturn(validToken);
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(storedRefreshToken));

        assertThatThrownBy(() -> refreshTokenService.refreshTokens(request))
                .isInstanceOf(RefreshTokenInvalidException.class)
                .hasMessageContaining("already used");

        verify(tokenDeserializer).deserialize("usedToken");
        verify(refreshTokenRepository).findById(tokenId);
    }

    @Test
    void refreshTokens_RevokedToken_ThrowsException() {
        RefreshRequestDTO request = new RefreshRequestDTO("revokedToken");
        storedRefreshToken.setRevoked(true);

        when(tokenDeserializer.deserialize("revokedToken")).thenReturn(validToken);
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(storedRefreshToken));

        assertThatThrownBy(() -> refreshTokenService.refreshTokens(request))
                .isInstanceOf(RefreshTokenInvalidException.class)
                .hasMessageContaining("revoked");

        verify(tokenDeserializer).deserialize("revokedToken");
        verify(refreshTokenRepository).findById(tokenId);
    }

    @Test
    void refreshTokens_UserNotFound_ThrowsException() {
        RefreshRequestDTO request = new RefreshRequestDTO("validToken");

        when(tokenDeserializer.deserialize("validToken")).thenReturn(validToken);
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(storedRefreshToken));
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.refreshTokens(request))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(tokenDeserializer).deserialize("validToken");
        verify(refreshTokenRepository, times(2)).findById(tokenId); // Called twice: findByTokenId() and markAsUsed()
        verify(accountRepository).findByEmail("test@example.com");
    }

    @Test
    void markAsUsed_Success() {
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(storedRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(storedRefreshToken);

        refreshTokenService.markAsUsed(tokenId);

        verify(refreshTokenRepository).findById(tokenId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void revokeRefreshToken_Success() {
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(storedRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(storedRefreshToken);

        refreshTokenService.revokeRefreshToken(tokenId);

        verify(refreshTokenRepository).findById(tokenId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void revokeRefreshTokenByString_Success() {
        String tokenString = "validRefreshToken";
        when(tokenDeserializer.deserialize(tokenString)).thenReturn(validToken);
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(storedRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(storedRefreshToken);

        refreshTokenService.revokeRefreshTokenByString(tokenString);

        verify(tokenDeserializer).deserialize(tokenString);
        verify(refreshTokenRepository).findById(tokenId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void revokeRefreshTokenByString_InvalidToken_ThrowsException() {
        String invalidToken = "invalidToken";
        when(tokenDeserializer.deserialize(invalidToken)).thenThrow(new RuntimeException("Invalid"));

        assertThatThrownBy(() -> refreshTokenService.revokeRefreshTokenByString(invalidToken))
                .isInstanceOf(InvalidJwtToken.class)
                .hasMessageContaining("Invalid refresh token format");

        verify(tokenDeserializer).deserialize(invalidToken);
        verify(refreshTokenRepository, never()).findById(any());
    }
}

