package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.AgeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgeGroupRepository extends JpaRepository<AgeGroup, Long> {
    Optional<AgeGroup> findByNameIgnoreCase(String name);
}
