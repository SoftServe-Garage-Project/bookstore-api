package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.TopUpDTO;
import com.softserve.bookstoreapi.dto.TransactionDTO;
import com.softserve.bookstoreapi.model.Transaction;
import com.softserve.bookstoreapi.service.impl.BalanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
@Controller
@RequestMapping("/api")
public class BalanceController {
    private final BalanceService balanceService;
    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

@PostMapping("/topUp")
    public ResponseEntity<TransactionDTO> BalanceTopUp(@Valid @RequestBody TopUpDTO request, Principal principal){
        TransactionDTO response = balanceService.topUpBalance(request, principal);
        return ResponseEntity.ok(response);

    }
}
