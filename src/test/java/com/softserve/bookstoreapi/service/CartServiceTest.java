package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.CartDTO;
import com.softserve.bookstoreapi.dto.CartItemRequestDTO;
import com.softserve.bookstoreapi.dto.CartItemResponseDTO;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.Book;
import com.softserve.bookstoreapi.model.Cart;
import com.softserve.bookstoreapi.model.CartItem;
import com.softserve.bookstoreapi.repository.AccountRepository;
import com.softserve.bookstoreapi.repository.BookRepository;
import com.softserve.bookstoreapi.repository.CartItemRepository;
import com.softserve.bookstoreapi.repository.CartRepository;
import com.softserve.bookstoreapi.service.impl.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private AccountRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private final String EMAIL = "user@test.com";
    @Test
    @DisplayName("addItem: Should create new item, reduce stock and return DTO")
    void addItem_Success_NewItem() {
        CartItemRequestDTO request = new CartItemRequestDTO(1L, 2);

        Account account = new Account();
        account.setEmail(EMAIL);

        Cart cart = new Cart();
        cart.setUser(account);
        cart.setCartItems(new ArrayList<>());

        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setPrice(new BigDecimal("10.00"));
        book.setStockQuantity(10);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(account));
        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> {
            CartItem item = inv.getArgument(0);
            item.setId(100L);
            return item;
        });

        CartItemResponseDTO result = cartService.addItemToCart(EMAIL, request);
        assertThat(result.quantity()).isEqualTo(2);
        assertThat(result.bookTitle()).isEqualTo("Test Book");

        assertThat(book.getStockQuantity()).isEqualTo(8);
        verify(bookRepository).save(book);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addItem: Should update existing item quantity and reduce stock")
    void addItem_Success_ExistingItem() {
        CartItemRequestDTO request = new CartItemRequestDTO(1L, 3);

        Book book = new Book();
        book.setId(1L);
        book.setStockQuantity(50);
        book.setPrice(BigDecimal.TEN);

        CartItem existingItem = new CartItem();
        existingItem.setBook(book);
        existingItem.setQuantity(5);
        existingItem.setId(55L);

        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>(List.of(existingItem)));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new Account()));
        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItemResponseDTO result = cartService.addItemToCart(EMAIL, request);

        assertThat(result.quantity()).isEqualTo(8);
        assertThat(book.getStockQuantity()).isEqualTo(47);

        assertThat(result.id()).isEqualTo(55L);
    }

    @Test
    @DisplayName("addItem: Should throw exception if not enough stock")
    void addItem_NotEnoughStock() {
        CartItemRequestDTO request = new CartItemRequestDTO(1L, 10);

        Book book = new Book();
        book.setId(1L);
        book.setStockQuantity(5);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new Account()));
        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(new Cart()));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        assertThatThrownBy(() -> cartService.addItemToCart(EMAIL, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not enough books");

        assertThat(book.getStockQuantity()).isEqualTo(5);
        verify(bookRepository, never()).save(any());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("addItem: Should throw exception if user not found")
    void addItem_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        CartItemRequestDTO req = new CartItemRequestDTO(1L, 1);

        assertThatThrownBy(() -> cartService.addItemToCart("unknown", req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("remove: Should delete item and restore stock")
    void removeCartItem_Success() {
        // GIVEN
        Long itemId = 99L;
        Book book = new Book();
        book.setId(1L);
        book.setStockQuantity(10);

        Account user = new Account();
        user.setEmail(EMAIL);

        Cart cart = new Cart();
        cart.setUser(user);

        CartItem item = new CartItem();
        item.setId(itemId);
        item.setBook(book);
        item.setQuantity(2);
        item.setCart(cart);

        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        cartService.removeCartItem(itemId, EMAIL);

        assertThat(book.getStockQuantity()).isEqualTo(12);
        verify(bookRepository).save(book);
        verify(cartItemRepository).delete(item);
    }

    @Test
    @DisplayName("remove: Should throw Access Denied if removing another user's item")
    void removeCartItem_AccessDenied() {
        Account owner = new Account();
        owner.setEmail("owner@test.com");

        Cart cart = new Cart();
        cart.setUser(owner);

        CartItem item = new CartItem();
        item.setCart(cart);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.removeCartItem(1L, "hacker@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied");

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getCart: Should return correct total price and update items timestamp")
    void getUserCart_Success() {
        Book b1 = new Book(); b1.setId(1L); b1.setPrice(new BigDecimal("10.00"));
        Book b2 = new Book(); b2.setId(2L); b2.setPrice(new BigDecimal("20.00"));

        CartItem i1 = new CartItem(); i1.setBook(b1); i1.setQuantity(2);
        CartItem i2 = new CartItem(); i2.setBook(b2); i2.setQuantity(1);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setCartItems(List.of(i1, i2));

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));

        CartDTO dto = cartService.getUserCart(EMAIL);

        assertThat(dto.totalPrice()).isEqualByComparingTo("40.00");
        assertThat(dto.items()).hasSize(2);

        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("getCart: Should return empty DTO if cart missing")
    void getUserCart_Empty() {
        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.empty());

        CartDTO result = cartService.getUserCart(EMAIL);

        assertThat(result.id()).isNull();
        assertThat(result.totalPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.items()).isEmpty();
    }
}