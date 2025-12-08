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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.Principal;
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

    @Test
    @DisplayName("topUpBalance: Should increase balance, create transaction and return DTO")
    void topUpBalance_Success() {
        String email = "user@test.com";
        BigDecimal initialBalance = new BigDecimal("50.00");
        BigDecimal topUpAmount = new BigDecimal("100.00");
        BigDecimal expectedBalance = initialBalance.add(topUpAmount);

        TopUpDTO request = new TopUpDTO(topUpAmount, "Sender", "Recipient");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Account account = new Account();
        account.setId(1L);
        account.setEmail(email);
        account.setBalance(initialBalance);

        TransactionDTO expectedDto = new TransactionDTO(
                10L, 1L, null, topUpAmount, TransactionType.DEPOSIT, TransactionStatus.COMPLETED
        );

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(expectedDto);

        TransactionDTO result = balanceService.topUpBalance(request, principal);

        assertThat(result).isEqualTo(expectedDto);

        assertThat(account.getBalance())
                .as("Balance should be updated correctly")
                .isEqualTo(expectedBalance);

        verify(accountRepository).save(account);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());

        Transaction savedTx = txCaptor.getValue();
        assertThat(savedTx.getAmount()).isEqualTo(topUpAmount);
        assertThat(savedTx.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(savedTx.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(savedTx.getSender()).isEqualTo(account);
    }

    @Test
    @DisplayName("topUpBalance: Should throw RuntimeException when account not found")
    void topUpBalance_AccountNotFound() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("unknown@test.com");

        when(accountRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        TopUpDTO request = new TopUpDTO(BigDecimal.TEN, "S", "R");

        assertThatThrownBy(() -> balanceService.topUpBalance(request, principal))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Account not found");

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}