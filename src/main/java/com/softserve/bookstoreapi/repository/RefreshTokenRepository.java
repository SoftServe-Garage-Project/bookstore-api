package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenId(UUID tokenId);

    /**
     * Deletes all refresh tokens that have expired (expiresAt is before the given timestamp)
     * @param now current timestamp
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Deletes all refresh tokens that are both used and revoked, and have expired
     * @param now current timestamp
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.used = true AND rt.revoked = true AND rt.expiresAt < :now")
    int deleteUsedAndRevokedExpiredTokens(@Param("now") Instant now);
}

