package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.Author;
import com.softserve.bookstoreapi.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findById(Long id);


    Page<Book> findByGenreId(Long genreId, Pageable pageable);
}

