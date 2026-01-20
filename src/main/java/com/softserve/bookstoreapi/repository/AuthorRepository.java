package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.Author;
import com.softserve.bookstoreapi.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);


}

