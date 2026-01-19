package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.model.CartItem;
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

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredItems() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(60);

        List<CartItem> expiredItems = cartItemRepository.findAllByUpdatedAtBefore(expirationTime);
        cartItemRepository.deleteAll(expiredItems);

        System.out.println("Cleaned up " + expiredItems.size() + " expired cart items.");
    }
}