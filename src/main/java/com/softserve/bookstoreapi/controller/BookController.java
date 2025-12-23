package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.BookDTO;
import com.softserve.bookstoreapi.mapper.BookMapper;
import com.softserve.bookstoreapi.model.Book;
import com.softserve.bookstoreapi.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;


@RestController
public class BookController {
    private final BookService bookService;
    private final BookMapper bookMapper;
    public BookController(BookService bookService, BookMapper bookMapper) {

        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }
    @PostMapping("/api/book")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO request) {
        Book created = bookService.createBook(request);
        BookDTO response = bookMapper.toDto(created);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/api/book")
    public Page<BookDTO> getBooks(
            @RequestParam(required = false) String genreName,
            @RequestParam(required = false) String title,
            Pageable pageable
    )
    {
        return bookService.getBooks(genreName, title ,pageable);
    }
}
