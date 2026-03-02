package com.softserve.bookstoreapi.repository;

import com.softserve.bookstoreapi.model.Author;
import com.softserve.bookstoreapi.model.Book;
import com.softserve.bookstoreapi.model.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findById(Long id);


    Page<Book> findByGenreId(Long genreId, Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCaseAndGenreId(String title, Long genreId, Pageable pageable);

    List<Book> findByTitleIgnoreCaseAndPublishedYearAndLanguage(String title, Integer publishedYear, Language language);

    boolean existsByGenreId(Long genreId);

    boolean existsByLanguageId(Long languageId);

    boolean existsByAgeGroupId(Long ageGroupId);

}

