package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.OrderDTO;
import com.softserve.bookstoreapi.dto.OrderItemDTO;
import com.softserve.bookstoreapi.model.*;
import com.softserve.bookstoreapi.model.enums.OrderStatus;
import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.model.enums.TransactionStatus;
import com.softserve.bookstoreapi.model.enums.TransactionType;
import com.softserve.bookstoreapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final AccountRepository accountRepository;
    private final BookRepository bookRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public OrderDTO checkout(String userEmail) {

        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Account account = cart.getUser();

        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        List<Book> booksToUpdate = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = new Order();
        order.setAccount(account);
        order.setStatus(OrderStatus.PAID);
        order.setPaymentMethod(PaymentMethod.BALANCE);

        for (CartItem cartItem : cart.getCartItems()) {
            Book book = cartItem.getBook();

            if (book.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Out of stock: " + book.getTitle());
            }

            book.setStockQuantity(book.getStockQuantity() - cartItem.getQuantity());
            booksToUpdate.add(book);

            BigDecimal originalPrice = book.getPrice();
            BigDecimal bookDiscount = BigDecimal.ZERO;
            BigDecimal promoDiscount = BigDecimal.ZERO;

            BigDecimal totalDiscountPercent = bookDiscount.add(promoDiscount);
            BigDecimal discountAmount = originalPrice
                    .multiply(totalDiscountPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal finalUnitPrice = originalPrice.subtract(discountAmount);

            BigDecimal lineItemTotal = finalUnitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalOrderAmount = totalOrderAmount.add(lineItemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOriginalPrice(originalPrice);
            orderItem.setBookDiscountPercentage(bookDiscount);
            orderItem.setPromoDiscountPercentage(promoDiscount);
            orderItem.setFinalPrice(finalUnitPrice);

            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalOrderAmount);
        order.setItems(orderItems);

        if (account.getBalance().compareTo(totalOrderAmount) < 0) {
            throw new RuntimeException("Insufficient funds.");
        }

        bookRepository.saveAll(booksToUpdate);

        account.setBalance(account.getBalance().subtract(totalOrderAmount));
        accountRepository.save(account);

        Order savedOrder = orderRepository.save(order);

        createTransactionRecord(account, savedOrder, totalOrderAmount);

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return mapToDto(savedOrder);
    }

    private void createTransactionRecord(Account sender, Order order, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(null);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.PURCHASE);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setPaymentMethod(PaymentMethod.BALANCE);
        transaction.setOrder(order);
        transaction.setDescription("Payment for Order #" + order.getId());

        transactionRepository.save(transaction);
    }

    private OrderDTO mapToDto(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(i -> new OrderItemDTO(
                        i.getBook().getId(),
                        i.getBook().getTitle(),
                        i.getQuantity(),
                        i.getOriginalPrice(),
                        i.getFinalPrice(),
                        i.getBookDiscountPercentage()
                )).toList();

        return new OrderDTO(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                items
        );
    }
}