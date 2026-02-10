package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.Book;
import com.softserve.bookstoreapi.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByAccountAndBook(Account account, Book book);

    Page<Review> findAllByBookId(Long bookId, Pageable pageable);
}