package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.BuyNowRequestDTO;
import com.softserve.bookstoreapi.dto.OrderDTO;
import com.softserve.bookstoreapi.model.*;
import com.softserve.bookstoreapi.model.enums.OrderStatus;
import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.repository.*;
import com.softserve.bookstoreapi.service.impl.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private BookRepository bookRepository;
    @Mock private TransactionRepository transactionRepository;

    @Mock private PromoCodeService promoCodeService;
    @Mock private PromoCodeRepository promoCodeRepository;

    @InjectMocks
    private OrderService orderService;

    private final String EMAIL = "user@test.com";
    private Account mockAccount;
    private Book mockBook;

    @BeforeEach
    void setUp() {
        mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setEmail(EMAIL);
        mockAccount.setBalance(new BigDecimal("1000.00"));

        mockBook = new Book();
        mockBook.setId(10L);
        mockBook.setTitle("Java Guide");
        mockBook.setPrice(new BigDecimal("100.00"));
        mockBook.setStockQuantity(20);
    }

    @Test
    @DisplayName("checkout: Success scenario (No PromoCode)")
    void checkout_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setBook(mockBook);
        cartItem.setQuantity(2);

        Cart cart = new Cart();
        cart.setUser(mockAccount);
        cart.setCartItems(new ArrayList<>(List.of(cartItem)));

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            order.setId(555L);
            order.setCreatedAt(java.time.LocalDateTime.now());
            return order;
        });

        OrderDTO result = orderService.checkout(EMAIL, null);

        assertThat(result.totalAmount()).isEqualByComparingTo("200.00");
        assertThat(result.status()).isEqualTo(OrderStatus.PAID);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.BALANCE);
        assertThat(result.items()).hasSize(1);

        assertThat(mockAccount.getBalance()).isEqualByComparingTo("800.00");
        verify(accountRepository).save(mockAccount);

        assertThat(mockBook.getStockQuantity()).isEqualTo(18);
        verify(bookRepository).saveAll(anyList());

        verify(transactionRepository).save(any(Transaction.class));

        verify(cartItemRepository).deleteAll(anyList());
        verify(cartRepository).save(cart);
        assertThat(cart.getCartItems()).isEmpty();
    }

    @Test
    @DisplayName("checkout: Fail - Cart Not Found")
    void checkout_CartNotFound() {
        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.checkout(EMAIL, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart not found");
    }

    @Test
    @DisplayName("checkout: Fail - Cart Is Empty")
    void checkout_CartEmpty() {
        Cart cart = new Cart();
        cart.setCartItems(Collections.emptyList());
        cart.setUser(mockAccount);

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.checkout(EMAIL, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart is empty");
    }

    @Test
    @DisplayName("checkout: Fail - Out Of Stock")
    void checkout_OutOfStock() {
        mockBook.setStockQuantity(1);

        CartItem cartItem = new CartItem();
        cartItem.setBook(mockBook);
        cartItem.setQuantity(5);

        Cart cart = new Cart();
        cart.setUser(mockAccount);
        cart.setCartItems(List.of(cartItem));

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.checkout(EMAIL, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Out of stock");

        verify(orderRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkout: Fail - Insufficient Funds")
    void checkout_InsufficientFunds() {
        mockAccount.setBalance(new BigDecimal("50.00"));

        CartItem cartItem = new CartItem();
        cartItem.setBook(mockBook);
        cartItem.setQuantity(1);

        Cart cart = new Cart();
        cart.setUser(mockAccount);
        cart.setCartItems(List.of(cartItem));

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.checkout(EMAIL, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient funds.");

        verify(orderRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("buyNow: Success scenario (No PromoCode)")
    void buyNow_Success() {
        BuyNowRequestDTO request = new BuyNowRequestDTO(10L, 3, null);

        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockAccount));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(mockBook));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(777L);
            return o;
        });

        OrderDTO result = orderService.buyNow(request, EMAIL);

        assertThat(result.totalAmount()).isEqualByComparingTo("300.00");
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).quantity()).isEqualTo(3);

        assertThat(mockAccount.getBalance()).isEqualByComparingTo("700.00");
        verify(accountRepository).save(mockAccount);

        assertThat(mockBook.getStockQuantity()).isEqualTo(17);
        verify(bookRepository).save(mockBook);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("buyNow: Fail - Book Not Found")
    void buyNow_BookNotFound() {
        BuyNowRequestDTO request = new BuyNowRequestDTO(999L, 1, null);

        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockAccount));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.buyNow(request, EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    @DisplayName("buyNow: Fail - Out Of Stock")
    void buyNow_OutOfStock() {
        mockBook.setStockQuantity(2);
        BuyNowRequestDTO request = new BuyNowRequestDTO(10L, 5, null);

        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockAccount));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(mockBook));

        assertThatThrownBy(() -> orderService.buyNow(request, EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not enough stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("buyNow: Fail - Insufficient Funds")
    void buyNow_InsufficientFunds() {
        mockAccount.setBalance(BigDecimal.ZERO);
        BuyNowRequestDTO request = new BuyNowRequestDTO(10L, 1, null);

        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockAccount));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(mockBook));

        assertThatThrownBy(() -> orderService.buyNow(request, EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient funds");

        verify(transactionRepository, never()).save(any());
    }
}