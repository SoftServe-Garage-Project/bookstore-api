package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.TransactionDTO;
import com.softserve.bookstoreapi.dto.TransactionResponseDTO;
import com.softserve.bookstoreapi.mapper.BookMapper;
import com.softserve.bookstoreapi.service.BookService;
import com.softserve.bookstoreapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @GetMapping
    public ResponseEntity<Page<TransactionResponseDTO>> getMyTransactions(Principal principal, @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TransactionResponseDTO> transactions = transactionService.getMyTransactions(principal.getName(), pageable);
        return ResponseEntity.ok(transactions);
    }
}