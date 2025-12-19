package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.repository.DeactivatedTokenRepository;
import com.softserve.bookstoreapi.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenCleanupSchedulerTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private DeactivatedTokenRepository deactivatedTokenRepository;

    @Captor
    private ArgumentCaptor<Instant> instantCaptor;

    private TokenCleanupScheduler tokenCleanupScheduler;

    @BeforeEach
    void setUp() {
        tokenCleanupScheduler = new TokenCleanupScheduler(refreshTokenRepository, deactivatedTokenRepository);
    }

    @Test
    void cleanupExpiredTokens_ShouldDeleteExpiredRefreshAndDeactivatedTokens() {
        when(refreshTokenRepository.deleteExpiredTokens(any(Instant.class))).thenReturn(5);
        when(deactivatedTokenRepository.deleteExpiredDeactivatedTokens(any(Instant.class))).thenReturn(3);

        tokenCleanupScheduler.cleanupExpiredTokens();

        verify(refreshTokenRepository, times(1)).deleteExpiredTokens(any(Instant.class));
        verify(deactivatedTokenRepository, times(1)).deleteExpiredDeactivatedTokens(any(Instant.class));
    }

    @Test
    void cleanupExpiredTokens_ShouldUseCurrentTimestamp() {
        Instant beforeCall = Instant.now();
        when(refreshTokenRepository.deleteExpiredTokens(any(Instant.class))).thenReturn(0);
        when(deactivatedTokenRepository.deleteExpiredDeactivatedTokens(any(Instant.class))).thenReturn(0);

        tokenCleanupScheduler.cleanupExpiredTokens();
        Instant afterCall = Instant.now();

        verify(refreshTokenRepository).deleteExpiredTokens(instantCaptor.capture());
        Instant capturedInstant = instantCaptor.getValue();

        assertThat(capturedInstant).isAfterOrEqualTo(beforeCall);
        assertThat(capturedInstant).isBeforeOrEqualTo(afterCall);
    }

    @Test
    void cleanupExpiredRefreshTokens_ShouldReturnDeletedCount() {
        Instant now = Instant.now();
        when(refreshTokenRepository.deleteExpiredTokens(now)).thenReturn(10);

        int result = tokenCleanupScheduler.cleanupExpiredRefreshTokens(now);

        assertThat(result).isEqualTo(10);
        verify(refreshTokenRepository, times(1)).deleteExpiredTokens(now);
    }

    @Test
    void cleanupExpiredDeactivatedTokens_ShouldReturnDeletedCount() {
        Instant now = Instant.now();
        when(deactivatedTokenRepository.deleteExpiredDeactivatedTokens(now)).thenReturn(7);

        int result = tokenCleanupScheduler.cleanupExpiredDeactivatedTokens(now);

        assertThat(result).isEqualTo(7);
        verify(deactivatedTokenRepository, times(1)).deleteExpiredDeactivatedTokens(now);
    }

    @Test
    void cleanupExpiredTokens_WhenNoTokensToDelete_ShouldCompleteSuccessfully() {
        when(refreshTokenRepository.deleteExpiredTokens(any(Instant.class))).thenReturn(0);
        when(deactivatedTokenRepository.deleteExpiredDeactivatedTokens(any(Instant.class))).thenReturn(0);

        tokenCleanupScheduler.cleanupExpiredTokens();

        verify(refreshTokenRepository, times(1)).deleteExpiredTokens(any(Instant.class));
        verify(deactivatedTokenRepository, times(1)).deleteExpiredDeactivatedTokens(any(Instant.class));
    }

    @Test
    void cleanupExpiredTokens_WhenRepositoryThrowsException_ShouldHandleGracefully() {
        when(refreshTokenRepository.deleteExpiredTokens(any(Instant.class)))
                .thenThrow(new RuntimeException("Database error"));

        tokenCleanupScheduler.cleanupExpiredTokens();

        verify(refreshTokenRepository, times(1)).deleteExpiredTokens(any(Instant.class));
    }
}

