package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.*;
import com.softserve.bookstoreapi.model.*;
import com.softserve.bookstoreapi.model.enums.OrderStatus;
import com.softserve.bookstoreapi.model.enums.PaymentMethod;
import com.softserve.bookstoreapi.model.enums.TransactionStatus;
import com.softserve.bookstoreapi.model.enums.TransactionType;
import com.softserve.bookstoreapi.repository.*;
import com.softserve.bookstoreapi.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final PromoCodeService promoCodeService;
    private final PromoCodeRepository promoCodeRepository;

    @Transactional
    public OrderDTO checkout(String userEmail, String promoCode) {

        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Account account = cart.getUser();

        BigDecimal baseTotalAmount = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            BigDecimal itemBaseTotal = item.getBook().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            baseTotalAmount = baseTotalAmount.add(itemBaseTotal);
        }

        BigDecimal promoDiscountPercent = BigDecimal.ZERO;
        PromoCode usedPromoCode = null;

        if (promoCode != null && !promoCode.isBlank()) {
            PromoCodeValidationRequestDTO validationRequest =
                    new PromoCodeValidationRequestDTO(promoCode, baseTotalAmount);

            PromoCodeValidationResponseDTO validationResponse =
                    promoCodeService.validatePromoCode(validationRequest);

            if (!validationResponse.valid()) {
                throw new RuntimeException("Invalid promo code: " + validationResponse.message());
            }

            promoDiscountPercent = validationResponse.discountPercentage();

            usedPromoCode = promoCodeRepository.findByCodeAndIsActiveTrue(promoCode)
                    .orElseThrow(() -> new RuntimeException("Promo code entity not found"));
        }

        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        List<Book> booksToUpdate = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = new Order();
        order.setAccount(account);
        order.setStatus(OrderStatus.PAID);
        order.setPaymentMethod(PaymentMethod.BALANCE);

        if (usedPromoCode != null) {
            order.setPromoCode(usedPromoCode);
        }

        for (CartItem cartItem : cart.getCartItems()) {
            Book book = cartItem.getBook();

            if (book.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Out of stock: " + book.getTitle());
            }

            book.setStockQuantity(book.getStockQuantity() - cartItem.getQuantity());
            booksToUpdate.add(book);

            BigDecimal originalPrice = book.getPrice();
            BigDecimal bookDiscount = BigDecimal.ZERO;

            BigDecimal totalDiscountPercent = bookDiscount.add(promoDiscountPercent);

            if (totalDiscountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
                totalDiscountPercent = BigDecimal.valueOf(100);
            }

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

            orderItem.setPromoDiscountPercentage(promoDiscountPercent);
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

        if (usedPromoCode != null) {
            promoCodeService.incrementUsage(usedPromoCode.getCode());
        }

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
    @Transactional
    public OrderDTO buyNow(BuyNowRequestDTO request, String userEmail) {

        Account account = accountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + request.bookId()));

        if (book.getStockQuantity() < request.quantity()) {
            throw new RuntimeException("Not enough stock for: " + book.getTitle() +
                    ". Available: " + book.getStockQuantity());
        }

        BigDecimal originalPrice = book.getPrice();
        BigDecimal bookDiscount = BigDecimal.ZERO;
        BigDecimal promoDiscountPercent = BigDecimal.ZERO;
        PromoCode usedPromoCode = null;

        if (request.promoCode() != null && !request.promoCode().isBlank()) {
            BigDecimal baseTotalAmount = originalPrice.multiply(BigDecimal.valueOf(request.quantity()));

            PromoCodeValidationRequestDTO validationRequest =
                    new PromoCodeValidationRequestDTO(request.promoCode(), baseTotalAmount);

            PromoCodeValidationResponseDTO validationResponse =
                    promoCodeService.validatePromoCode(validationRequest);

            if (!validationResponse.valid()) {
                throw new RuntimeException("Invalid promo code: " + validationResponse.message());
            }

            promoDiscountPercent = validationResponse.discountPercentage();

            usedPromoCode = promoCodeRepository.findByCodeAndIsActiveTrue(request.promoCode())
                    .orElseThrow(() -> new RuntimeException("Promo code entity not found"));
        }

        BigDecimal totalDiscountPercent = bookDiscount.add(promoDiscountPercent);

        if (totalDiscountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            totalDiscountPercent = BigDecimal.valueOf(100);
        }

        BigDecimal discountAmount = originalPrice
                .multiply(totalDiscountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalUnitPrice = originalPrice.subtract(discountAmount);
        BigDecimal totalOrderAmount = finalUnitPrice.multiply(BigDecimal.valueOf(request.quantity()));

        if (account.getBalance().compareTo(totalOrderAmount) < 0) {
            throw new RuntimeException("Insufficient funds. Balance: " + account.getBalance() +
                    ", Required: " + totalOrderAmount);
        }

        book.setStockQuantity(book.getStockQuantity() - request.quantity());
        bookRepository.save(book);

        account.setBalance(account.getBalance().subtract(totalOrderAmount));
        accountRepository.save(account);

        // 6. Создание заказа
        Order order = new Order();
        order.setAccount(account);
        order.setTotalAmount(totalOrderAmount);
        order.setStatus(OrderStatus.PAID);
        order.setPaymentMethod(PaymentMethod.BALANCE);

        if (usedPromoCode != null) {
            order.setPromoCode(usedPromoCode);
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setBook(book);
        orderItem.setQuantity(request.quantity());
        orderItem.setOriginalPrice(originalPrice);
        orderItem.setBookDiscountPercentage(bookDiscount);

        orderItem.setPromoDiscountPercentage(promoDiscountPercent);
        orderItem.setFinalPrice(finalUnitPrice);

        order.setItems(List.of(orderItem));
        Order savedOrder = orderRepository.save(order);

        createTransactionRecord(account, savedOrder, totalOrderAmount);

        if (usedPromoCode != null) {
            promoCodeService.incrementUsage(usedPromoCode.getCode());
        }

        return mapToDto(savedOrder);
    }


    @Transactional(readOnly = true)
    public Page<OrderDTO> getUserOrders(String userEmail, Pageable pageable) {
        Account account = accountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Page<Order> orders = orderRepository.findByAccount(account, pageable);

        return orders.map(this::mapToDto);
    }
}