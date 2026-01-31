package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByAccount(Account account, Pageable pageable);

    @Query("""
        SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END
        FROM Order o
        JOIN o.items i
        WHERE o.account.email = :userEmail
          AND i.book.id = :bookId
          AND o.status = 'PAID'
    """)
    boolean existsPaidOrderForBook(String userEmail, Long bookId);
}

