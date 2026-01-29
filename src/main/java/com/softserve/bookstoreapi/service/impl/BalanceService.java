package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.BalanceDTO;
import com.softserve.bookstoreapi.dto.TopUpDTO;
import com.softserve.bookstoreapi.dto.TransactionDTO;
import com.softserve.bookstoreapi.mapper.TransactionMapper;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.Transaction;
import com.softserve.bookstoreapi.model.enums.TransactionStatus;
import com.softserve.bookstoreapi.model.enums.TransactionType;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
public class BalanceService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    @Value("${app.system-account.email:provider@bookstore.com}")
    private String systemProviderEmail;

    public BalanceService(AccountRepository accountRepository, TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional
    public TransactionDTO topUpBalance(TopUpDTO request, Principal principal) {
        String userEmail = principal.getName();
        Account userAccount = accountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User account not found"));
        Account systemProvider = accountRepository.findByEmail(systemProviderEmail)
                .orElseThrow(() -> new RuntimeException("System provider account not found. Please contact support."));

        userAccount.setBalance(userAccount.getBalance().add(request.amount()));
        accountRepository.save(userAccount);
        Transaction tx = new Transaction();

        tx.setSender(systemProvider);
        tx.setReceiver(userAccount);

        tx.setAmount(request.amount());
        tx.setType(TransactionType.DEPOSIT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription("Balance top-up");

        transactionRepository.save(tx);

        return transactionMapper.toDto(tx);
    }
    @Transactional(readOnly = true)
    public BalanceDTO getCurrentBalance(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new BalanceDTO(account.getBalance(), "UAH");
    }
}