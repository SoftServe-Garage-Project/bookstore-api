package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.OrderDTO;
import com.softserve.bookstoreapi.service.impl.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> checkout(Principal principal) {
        OrderDTO order = orderService.checkout(principal.getName());
        return ResponseEntity.ok(order);
    }
}