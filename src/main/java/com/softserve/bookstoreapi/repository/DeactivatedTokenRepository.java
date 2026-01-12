package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.DeactivatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface DeactivatedTokenRepository extends JpaRepository<DeactivatedToken, UUID> {

    /**
     * Deletes all deactivated tokens whose keepUntil time has passed.
     * These tokens no longer need to be stored since the original JWT would have expired anyway.
     * @param now current timestamp
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM DeactivatedToken dt WHERE dt.keepUntil < :now")
    int deleteExpiredDeactivatedTokens(@Param("now") Instant now);
}
