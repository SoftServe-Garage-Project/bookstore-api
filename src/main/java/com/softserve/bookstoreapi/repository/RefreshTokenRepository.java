package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenId(UUID tokenId);

    /**
     * Count active (not used, not revoked, not expired) refresh tokens for a user.
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.userEmail = :userEmail " +
           "AND rt.used = false AND rt.revoked = false AND rt.expiresAt > :now")
    long countActiveTokensByUserEmail(@Param("userEmail") String userEmail, @Param("now") Instant now);

    /**
     * Find all active refresh tokens for a user, ordered by creation time (oldest first).
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userEmail = :userEmail " +
           "AND rt.used = false AND rt.revoked = false AND rt.expiresAt > :now " +
           "ORDER BY rt.createdAt ASC")
    List<RefreshToken> findActiveTokensByUserEmailOrderByCreatedAtAsc(
            @Param("userEmail") String userEmail, @Param("now") Instant now);
}

