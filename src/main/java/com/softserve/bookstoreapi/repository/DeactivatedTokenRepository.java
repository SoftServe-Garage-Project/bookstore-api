package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.DeactivatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface DeactivatedTokenRepository extends JpaRepository<DeactivatedToken, UUID> {
    void deleteByKeepUntilBefore(Instant now);
}
