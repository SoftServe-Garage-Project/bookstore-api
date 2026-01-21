package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.TransactionDTO;
import com.softserve.bookstoreapi.dto.TransactionResponseDTO;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.Transaction;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getMyTransactions(String userEmail, Pageable pageable) {
        Account user = accountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Page<Transaction> transactions = transactionRepository.findBySenderOrReceiver(user, user, pageable);

        return transactions.map(this::mapToDto);
    }

    private TransactionResponseDTO mapToDto(Transaction tx) {
        return new TransactionResponseDTO(
                tx.getId(),
                (tx.getSender() != null) ? tx.getSender().getId() : null,
                (tx.getReceiver() != null) ? tx.getReceiver().getId() : null,
                tx.getAmount(),
                tx.getType(),
                tx.getStatus(),
                tx.getPaymentMethod(),
                tx.getDescription(),
                (tx.getOrder() != null) ? tx.getOrder().getId() : null,
                tx.getCreatedAt()
        );
    }
}