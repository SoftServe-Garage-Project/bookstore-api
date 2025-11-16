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

    @Query("""
    SELECT b FROM Book b
    WHERE (:genreName IS NULL OR LOWER(b.genre.name) = LOWER(:genreName))
      AND (:authorName IS NULL OR EXISTS (
            SELECT a FROM b.authors a 
            WHERE LOWER(CONCAT(a.firstName, ' ', a.lastName)) = LOWER(:authorName)))
      AND (:ageGroupName IS NULL OR LOWER(b.ageGroup.name) = LOWER(:ageGroupName))
      AND (:languageName IS NULL OR LOWER(b.language.name) = LOWER(:languageName))
      AND (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
""")
    Page<Book> findFilteredBooks(
            @Param("genreName") String genreName,
            @Param("authorName") String authorName,
            @Param("ageGroupName") String ageGroupName,
            @Param("languageName") String languageName,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

