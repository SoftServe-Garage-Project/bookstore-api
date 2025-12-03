package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.TopUpDTO;
import com.softserve.bookstoreapi.dto.TransactionDTO;
import com.softserve.bookstoreapi.mapper.TransactionMapper;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.Transaction;
import com.softserve.bookstoreapi.model.enums.TransactionStatus;
import com.softserve.bookstoreapi.model.enums.TransactionType;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
@Service
public class BalanceService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public BalanceService(AccountRepository accountRepository, TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }
    @Transactional
    public TransactionDTO topUpBalance(TopUpDTO request, Principal principal) {
        String name = principal.getName();

        Account account = accountRepository.findByEmail(name)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setSender(account);
        tx.setAmount(request.amount());
        tx.setType(TransactionType.DEPOSIT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription("Balance top-up via stub gateway");

        transactionRepository.save(tx);

        return transactionMapper.toDto(tx);
    }
}
