package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByEmailAndIsActiveTrue(String email);
    boolean existsByEmail(String email);
}

