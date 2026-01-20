package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.BookDTO;
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
    private final BookMapper bookMapper;

    @Transactional
    public Book createBook(BookDTO request) {
        Book book = new Book();
        return bookRepository.save(fillBookData(book, request));
    }

    @Transactional
    public Book updateBook(Long id, BookDTO request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        Book updatedBook = fillBookData(book, request);
        return bookRepository.save(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }
    public List<Book> getBooks() {
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
        return books.map(bookMapper::toDto);
    }
    private Book fillBookData(Book book, BookDTO request) {
        Genre genre = genreRepository.findByNameIgnoreCase(request.genre())
                .orElseThrow(() -> new RuntimeException("Genre not found: " + request.genre()));
        AgeGroup ageGroup = ageGroupRepository.findByNameIgnoreCase(request.ageGroup())
                .orElseThrow(() -> new RuntimeException("Age group not found: " + request.ageGroup()));
        Language language = languageRepository.findByCodeIgnoreCase(request.languageCode())
                .orElseThrow(() -> new RuntimeException("Language not found: " + request.languageCode()));

        book.setTitle(request.title());
        book.setDescription(request.description());
        book.setGenre(genre);
        book.setAgeGroup(ageGroup);
        book.setPublishedYear(request.publishedYear());
        book.setLanguage(language);
        book.setPrice(request.price());
        book.setStockQuantity(request.stockQuantity());
        book.setDiscountPercentage(request.discountPercentage());
        book.setPageCount(request.pageCount());
        book.setCoverImageUrl(request.coverImageUrl());

        List<Author> authors = request.authors().stream()
                .map(a -> {
                    return authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(a.firstName(), a.lastName())
                            .orElseGet(() -> {
                                Author newAuthor = new Author();
                                newAuthor.setFirstName(a.firstName());
                                newAuthor.setLastName(a.lastName());
                                return authorRepository.save(newAuthor);
                            });
                })
                .collect(Collectors.toList());

        book.setAuthors(authors);
        return book;
    }
}
