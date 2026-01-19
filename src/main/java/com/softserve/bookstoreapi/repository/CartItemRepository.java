package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByUpdatedAtBefore(LocalDateTime dateTime);
}