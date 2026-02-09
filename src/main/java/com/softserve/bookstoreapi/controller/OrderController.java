package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.BuyNowRequestDTO;
import com.softserve.bookstoreapi.dto.CheckoutRequestDTO;
import com.softserve.bookstoreapi.dto.OrderDTO;
import com.softserve.bookstoreapi.service.impl.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> checkout(
            @RequestBody(required = false) CheckoutRequestDTO request, Principal principal) {
        String code = (request != null) ? request.promoCode() : null;
        OrderDTO order = orderService.checkout(principal.getName(), code);
        return ResponseEntity.ok(order);
    }
    @PostMapping("/buy-now")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDTO> buyNow(@Valid @RequestBody BuyNowRequestDTO request, Principal principal) {
        OrderDTO order = orderService.buyNow(request,principal.getName());
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<OrderDTO>> getMyOrders(
            Principal principal,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<OrderDTO> orders = orderService.getUserOrders(principal.getName(), pageable);
        return ResponseEntity.ok(orders);
    }
}