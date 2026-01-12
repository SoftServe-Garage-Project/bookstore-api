package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByIdentifier(String identifier);

    /**
     * Delete all blocked attempts where the block has expired
     */
    @Modifying
    void deleteByBlockedUntilBefore(LocalDateTime time);

    /**
     * Delete attempts that are older than the specified time and have no active block
     */
    @Modifying
    @Query("DELETE FROM LoginAttempt la WHERE la.lastAttemptTime < :threshold AND (la.blockedUntil IS NULL OR la.blockedUntil < :now)")
    int deleteExpiredAttempts(@Param("threshold") LocalDateTime threshold, @Param("now") LocalDateTime now);
}
