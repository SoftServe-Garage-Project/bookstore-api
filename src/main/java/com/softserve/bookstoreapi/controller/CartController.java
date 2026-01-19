package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.CartDTO;
import com.softserve.bookstoreapi.dto.CartItemRequestDTO;
import com.softserve.bookstoreapi.dto.CartItemResponseDTO;
import com.softserve.bookstoreapi.service.impl.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartItemResponseDTO> addToCart(
            @Valid @RequestBody CartItemRequestDTO request,
            Principal principal
    ) {
        CartItemResponseDTO response = cartService.addItemToCart(principal.getName(), request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long itemId, Principal principal) {
        cartService.removeCartItem(itemId, principal.getName());
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public ResponseEntity<CartDTO> getCart(Principal principal) {
        CartDTO cartDTO = cartService.getUserCart(principal.getName());
        return ResponseEntity.ok(cartDTO);
    }
}