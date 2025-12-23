package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.dto.TransactionDTO;
import com.softserve.bookstoreapi.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionDTO toDto(Transaction tx) {
        return new TransactionDTO(
                tx.getId(),
                tx.getSender() != null ? tx.getSender().getId() : null,
                tx.getReceiver() != null ? tx.getReceiver().getId() : null,
                tx.getAmount(),
                tx.getType(),
                tx.getStatus()
        );
    }
}