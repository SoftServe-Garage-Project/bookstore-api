package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.CartDTO;
import com.softserve.bookstoreapi.dto.CartItemRequestDTO;
import com.softserve.bookstoreapi.dto.CartItemResponseDTO;
import com.softserve.bookstoreapi.model.*;
import com.softserve.bookstoreapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final AccountRepository userRepository;

    @Transactional
    public CartItemResponseDTO addItemToCart(String userEmail, CartItemRequestDTO request) {
        Account user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        if (book.getStockQuantity() < request.quantity()) {
            throw new RuntimeException("Not enough books in stock. Available: " + book.getStockQuantity());
        }
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(book.getId()))
                .findFirst();

        CartItem savedItem;
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
            savedItem = cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setBook(book);
            newItem.setQuantity(request.quantity());
            cart.getCartItems().add(newItem);
            savedItem = cartItemRepository.save(newItem);
        }

        return new CartItemResponseDTO(
                savedItem.getId(),
                savedItem.getBook().getId(),
                savedItem.getBook().getTitle(),
                savedItem.getQuantity(),
                savedItem.getBook().getPrice()
        );
    }

    @Transactional
    public void removeCartItem(Long cartItemId, String userEmail) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getCart().getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied");
        }
        cartItemRepository.delete(item);
    }
    @Transactional
    public CartItemResponseDTO updateCartItemQuantity(Long cartItemId, int newQuantity, String userEmail) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied: You can only modify your own cart");
        }
        Book book = item.getBook();
        if (book.getStockQuantity() < newQuantity) {
            throw new RuntimeException("Not enough stock. Available: " + book.getStockQuantity());
        }

        item.setQuantity(newQuantity);
        CartItem savedItem = cartItemRepository.save(item);
        return new CartItemResponseDTO(
                savedItem.getId(),
                savedItem.getBook().getId(),
                savedItem.getBook().getTitle(),
                savedItem.getQuantity(),
                savedItem.getBook().getPrice()
        );
    }

    @Transactional
    public CartDTO getUserCart(String userEmail) {
        Cart cart = cartRepository.findByUserEmail(userEmail).orElse(null);

        if (cart == null) {
            return new CartDTO(null, List.of(), BigDecimal.ZERO);
        }

        List<CartItemResponseDTO> itemDTOs = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        for (CartItem item : cart.getCartItems()) {
            CartItemResponseDTO dto = new CartItemResponseDTO(
                    item.getId(),
                    item.getBook().getId(),
                    item.getBook().getTitle(),
                    item.getQuantity(),
                    item.getBook().getPrice()
            );
            itemDTOs.add(dto);
            BigDecimal itemTotal = item.getBook().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
            item.setUpdatedAt(now);
        }
        cartRepository.save(cart);

        return new CartDTO(cart.getId(), itemDTOs, totalPrice);
    }
}