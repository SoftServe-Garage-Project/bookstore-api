package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.TopUpDTO;
import com.softserve.bookstoreapi.dto.TransactionDTO;
import com.softserve.bookstoreapi.mapper.TransactionMapper;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.Transaction;
import com.softserve.bookstoreapi.model.enums.TransactionStatus;
import com.softserve.bookstoreapi.model.enums.TransactionType;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.repository.TransactionRepository;
import com.softserve.bookstoreapi.service.impl.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceService Tests")
class BalanceServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private BalanceService balanceService;
    private final String SYSTEM_EMAIL = "provider@bookstore.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(balanceService, "systemProviderEmail", SYSTEM_EMAIL);
    }

    @Test
    @DisplayName("topUpBalance: Should increase balance, create transaction (Provider -> User) and return DTO")
    void topUpBalance_Success() {
        // --- GIVEN ---
        String userEmail = "user@test.com";
        BigDecimal initialBalance = new BigDecimal("50.00");
        BigDecimal topUpAmount = new BigDecimal("100.00");
        BigDecimal expectedBalance = initialBalance.add(topUpAmount);

        TopUpDTO request = new TopUpDTO(topUpAmount, "Sender", "Recipient");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userEmail);

        Account userAccount = new Account();
        userAccount.setId(1L);
        userAccount.setEmail(userEmail);
        userAccount.setBalance(initialBalance);

        Account systemAccount = new Account();
        systemAccount.setId(999L);
        systemAccount.setEmail(SYSTEM_EMAIL);

        when(accountRepository.findByEmail(userEmail)).thenReturn(Optional.of(userAccount));
        when(accountRepository.findByEmail(SYSTEM_EMAIL)).thenReturn(Optional.of(systemAccount));

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionDTO expectedDto = new TransactionDTO(
                10L,
                systemAccount.getId(), // Sender = System
                userAccount.getId(),   // Receiver = User
                topUpAmount,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED
        );
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(expectedDto);

        TransactionDTO result = balanceService.topUpBalance(request, principal);

        assertThat(result).isEqualTo(expectedDto);

        assertThat(userAccount.getBalance())
                .as("Balance should be updated correctly")
                .isEqualTo(expectedBalance);

        verify(accountRepository).save(userAccount);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());

        Transaction savedTx = txCaptor.getValue();
        assertThat(savedTx.getAmount()).isEqualTo(topUpAmount);
        assertThat(savedTx.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(savedTx.getStatus()).isEqualTo(TransactionStatus.COMPLETED);

        assertThat(savedTx.getSender()).isEqualTo(systemAccount);
        assertThat(savedTx.getReceiver()).isEqualTo(userAccount);
    }

    @Test
    @DisplayName("topUpBalance: Should throw RuntimeException when user account not found")
    void topUpBalance_AccountNotFound() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("unknown@test.com");

        when(accountRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        TopUpDTO request = new TopUpDTO(BigDecimal.TEN, "S", "R");
        assertThatThrownBy(() -> balanceService.topUpBalance(request, principal))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User account not found"); // <-- ИСПРАВЛЕНО ТУТ

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("topUpBalance: Should throw RuntimeException when system provider account not found")
    void topUpBalance_SystemProviderNotFound() {
        String userEmail = "user@test.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userEmail);

        Account userAccount = new Account();
        userAccount.setEmail(userEmail);

        when(accountRepository.findByEmail(userEmail)).thenReturn(Optional.of(userAccount));
        when(accountRepository.findByEmail(SYSTEM_EMAIL)).thenReturn(Optional.empty());

        TopUpDTO request = new TopUpDTO(BigDecimal.TEN, "S", "R");

        assertThatThrownBy(() -> balanceService.topUpBalance(request, principal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("System provider account not found");

        verify(transactionRepository, never()).save(any());
    }
}