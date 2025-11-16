package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.DTO.BookDTO;
import com.softserve.bookstoreapi.mapper.BookMapper;
import com.softserve.bookstoreapi.model.*;
import com.softserve.bookstoreapi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final AgeGroupRepository ageGroupRepository;
    private final LanguageRepository languageRepository;
    private final AuthorRepository authorRepository;

    @Transactional
    public Book createBook(BookDTO request) {
        Genre genre = genreRepository.findByNameIgnoreCase(request.getGenre())
                .orElseThrow(() -> new RuntimeException("Genre not found"));
        AgeGroup ageGroup = ageGroupRepository.findByNameIgnoreCase(request.getAgeGroup())
                .orElseThrow(() -> new RuntimeException("Age group not found"));
        Language language = languageRepository.findByCodeIgnoreCase(request.getLanguageCode())
                .orElseThrow(() -> new RuntimeException("Language not found"));

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setGenre(genre);
        book.setAgeGroup(ageGroup);
        book.setPublishedYear(request.getPublishedYear());
        book.setLanguage(language);
        book.setPrice(request.getPrice());
        book.setStockQuantity(request.getStockQuantity());
        book.setDiscountPercentage(request.getDiscountPercentage());
        book.setPageCount(request.getPageCount());
        book.setCoverImageUrl(request.getCoverImageUrl());

        List<Author> authors = request.getAuthors().stream()
                .map(a -> {
                    Author author = new Author();
                    author.setFirstName(a.getFirstName());
                    author.setLastName(a.getLastName());
                    author.setBiography(null);
                    author.setCountry(null);
                    author.setPhotoUrl(null);
                    return authorRepository.save(author);
                })
                .collect(Collectors.toList());

        book.setAuthors(authors);

        return bookRepository.save(book);
    }
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }


    public Page<BookDTO> getBooks(String genreName, String title, Pageable pageable) {
        if (genreName != null) genreName = genreName.trim();
        if (title != null) title = title.trim();
        Page<Book> books;

        // 1 title is empty --> findByGenre
        if ((title == null || title.isBlank()) && genreName != null && !genreName.isBlank()) {
            Genre genre = genreRepository.findByNameIgnoreCase(genreName).orElse(null);
            if (genre == null) return Page.empty(pageable);
            books = bookRepository.findByGenreId(genre.getId(), pageable);
        }
        // 2 genre is empty, FindByTittle
        else if ((genreName == null || genreName.isBlank()) && title != null && !title.isBlank()) {
            books = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        }
        // 3. both is not empty
        else if (title != null && !title.isBlank() && genreName != null && !genreName.isBlank()) {
            Genre genre = genreRepository.findByNameIgnoreCase(genreName).orElse(null);
            if (genre == null) return Page.empty(pageable);

            books = bookRepository.findByTitleContainingIgnoreCaseAndGenreId(title, genre.getId(), pageable);

            // If a name match is found, but the genre does not match → blank page
            if (books.isEmpty()) return Page.empty(pageable);
        }
        // 4 tittle = empty genre = empty
        else {
            books = bookRepository.findAll(pageable);
        }

        return books.map(BookMapper::toDto);
    }
}
