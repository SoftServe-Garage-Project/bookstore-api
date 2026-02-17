package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.BuyNowRequestDTO;
import com.softserve.bookstoreapi.dto.CheckoutRequestDTO;
import com.softserve.bookstoreapi.dto.OrderDTO;
import com.softserve.bookstoreapi.dto.OrderStatusUpdateDTO;
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
    public ResponseEntity<OrderDTO> checkout(@Valid @RequestBody CheckoutRequestDTO request, Principal principal) {
        OrderDTO order = orderService.checkout(principal.getName(), request);
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
    public ResponseEntity<Page<OrderDTO>> getMyOrders(Principal principal, @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<OrderDTO> orders = orderService.getUserOrders(principal.getName(), pageable);
        return ResponseEntity.ok(orders);
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateDTO request) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, request.status());
        return ResponseEntity.ok(updatedOrder);
    }
}