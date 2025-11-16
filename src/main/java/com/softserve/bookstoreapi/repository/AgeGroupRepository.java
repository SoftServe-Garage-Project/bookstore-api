package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.AgeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AgeGroupRepository extends JpaRepository<AgeGroup, Long> {
    Optional<AgeGroup> findByNameIgnoreCase(String name);
}
