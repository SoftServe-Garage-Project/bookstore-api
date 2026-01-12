package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction ,Long> {
}
