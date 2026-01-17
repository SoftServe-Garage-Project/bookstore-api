package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    @Query("SELECT p FROM PromoCode p WHERE p.code = :code AND p.isActive = true")
    Optional<PromoCode> findByCodeAndIsActiveTrue(@Param("code") String code);

    Optional<PromoCode> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT p FROM PromoCode p WHERE p.validTo < :now AND p.isActive = true")
    Iterable<PromoCode> findExpiredPromoCodes(@Param("now") LocalDateTime now);
}
