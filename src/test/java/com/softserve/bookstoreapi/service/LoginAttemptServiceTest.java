package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.model.LoginAttempt;
import com.softserve.bookstoreapi.repository.LoginAttemptRepository;
import com.softserve.bookstoreapi.service.impl.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private static final String TEST_IDENTIFIER = "192.168.1.1";
    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 15;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loginAttemptService, "maxAttempts", MAX_ATTEMPTS);
        ReflectionTestUtils.setField(loginAttemptService, "blockDurationMinutes", BLOCK_DURATION_MINUTES);
        ReflectionTestUtils.setField(loginAttemptService, "attemptExpiryHours", 24);
    }

    @Test
    void loginFailed_FirstAttempt_CreatesNewRecord() {
        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.empty());
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArgument(0));

        loginAttemptService.loginFailed(TEST_IDENTIFIER);

        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(captor.capture());

        LoginAttempt saved = captor.getValue();
        assertThat(saved.getIdentifier()).isEqualTo(TEST_IDENTIFIER);
        assertThat(saved.getAttempts()).isEqualTo(1);
        assertThat(saved.getBlockedUntil()).isNull();
    }

    @Test
    void loginFailed_MaxAttemptsReached_BlocksIdentifier() {
        LoginAttempt existingAttempt = LoginAttempt.builder()
                .identifier(TEST_IDENTIFIER)
                .attempts(MAX_ATTEMPTS - 1)
                .lastAttemptTime(LocalDateTime.now().minusMinutes(1))
                .build();

        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(existingAttempt));
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArgument(0));

        loginAttemptService.loginFailed(TEST_IDENTIFIER);

        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(captor.capture());

        LoginAttempt saved = captor.getValue();
        assertThat(saved.getAttempts()).isEqualTo(MAX_ATTEMPTS);
        assertThat(saved.getBlockedUntil()).isNotNull();
        assertThat(saved.getBlockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void loginSucceeded_ExistingAttempt_DeletesRecord() {
        LoginAttempt existingAttempt = LoginAttempt.builder()
                .identifier(TEST_IDENTIFIER)
                .attempts(2)
                .lastAttemptTime(LocalDateTime.now().minusMinutes(1))
                .build();

        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(existingAttempt));

        loginAttemptService.loginSucceeded(TEST_IDENTIFIER);

        verify(loginAttemptRepository).delete(existingAttempt);
    }

    @Test
    void loginSucceeded_NoExistingAttempt_DoesNothing() {
        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.empty());

        loginAttemptService.loginSucceeded(TEST_IDENTIFIER);

        verify(loginAttemptRepository, never()).delete(any());
    }

    @Test
    void isBlocked_ActiveBlock_ReturnsTrue() {
        LoginAttempt blockedAttempt = LoginAttempt.builder()
                .identifier(TEST_IDENTIFIER)
                .attempts(MAX_ATTEMPTS)
                .lastAttemptTime(LocalDateTime.now())
                .blockedUntil(LocalDateTime.now().plusMinutes(10))
                .build();

        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(blockedAttempt));

        boolean result = loginAttemptService.isBlocked(TEST_IDENTIFIER);

        assertThat(result).isTrue();
        verify(loginAttemptRepository, never()).delete(any());
    }

    @Test
    void isBlocked_ExpiredBlock_ReturnsFalseAndCleansUp() {
        LoginAttempt expiredBlockAttempt = LoginAttempt.builder()
                .identifier(TEST_IDENTIFIER)
                .attempts(MAX_ATTEMPTS)
                .lastAttemptTime(LocalDateTime.now().minusMinutes(30))
                .blockedUntil(LocalDateTime.now().minusMinutes(1))
                .build();

        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(expiredBlockAttempt));

        boolean result = loginAttemptService.isBlocked(TEST_IDENTIFIER);

        assertThat(result).isFalse();
        verify(loginAttemptRepository).delete(expiredBlockAttempt);
    }

    @Test
    void isBlocked_NoRecord_ReturnsFalse() {
        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.empty());

        boolean result = loginAttemptService.isBlocked(TEST_IDENTIFIER);

        assertThat(result).isFalse();
    }

    @Test
    void getRemainingAttempts_NoRecord_ReturnsMaxAttempts() {
        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.empty());

        int remaining = loginAttemptService.getRemainingAttempts(TEST_IDENTIFIER);

        assertThat(remaining).isEqualTo(MAX_ATTEMPTS);
    }

    @Test
    void getRemainingAttempts_ExistingAttempts_ReturnsCorrectCount() {
        LoginAttempt attempt = LoginAttempt.builder()
                .identifier(TEST_IDENTIFIER)
                .attempts(3)
                .lastAttemptTime(LocalDateTime.now())
                .build();

        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(attempt));

        int remaining = loginAttemptService.getRemainingAttempts(TEST_IDENTIFIER);

        assertThat(remaining).isEqualTo(2);
    }

    @Test
    void getBlockExpirationTime_ActiveBlock_ReturnsTime() {
        LocalDateTime blockedUntil = LocalDateTime.now().plusMinutes(10);
        LoginAttempt blockedAttempt = LoginAttempt.builder()
                .identifier(TEST_IDENTIFIER)
                .attempts(MAX_ATTEMPTS)
                .lastAttemptTime(LocalDateTime.now())
                .blockedUntil(blockedUntil)
                .build();

        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(blockedAttempt));

        LocalDateTime result = loginAttemptService.getBlockExpirationTime(TEST_IDENTIFIER);

        assertThat(result).isEqualTo(blockedUntil);
    }

    @Test
    void getBlockExpirationTime_NoBlock_ReturnsNull() {
        when(loginAttemptRepository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.empty());

        LocalDateTime result = loginAttemptService.getBlockExpirationTime(TEST_IDENTIFIER);

        assertThat(result).isNull();
    }

    @Test
    void cleanupExpiredAttempts_ReturnsDeletedCount() {
        when(loginAttemptRepository.deleteExpiredAttempts(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5);

        int deleted = loginAttemptService.cleanupExpiredAttempts();

        assertThat(deleted).isEqualTo(5);
        verify(loginAttemptRepository).deleteExpiredAttempts(any(LocalDateTime.class), any(LocalDateTime.class));
    }
}

