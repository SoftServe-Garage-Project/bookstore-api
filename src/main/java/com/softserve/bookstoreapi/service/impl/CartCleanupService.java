package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.model.Book;
import com.softserve.bookstoreapi.model.CartItem;
import com.softserve.bookstoreapi.repository.BookRepository;
import com.softserve.bookstoreapi.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartCleanupService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredItems() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(30);

        List<CartItem> expiredItems = cartItemRepository.findAllByUpdatedAtBefore(expirationTime);

        for (CartItem item : expiredItems) {
            Book book = item.getBook();
            book.setStockQuantity(book.getStockQuantity() + item.getQuantity());
            bookRepository.save(book);
            cartItemRepository.delete(item);
        }
    }
}
