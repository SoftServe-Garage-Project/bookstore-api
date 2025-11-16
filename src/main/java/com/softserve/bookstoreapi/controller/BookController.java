package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.DTO.BookDTO;
import com.softserve.bookstoreapi.model.Book;
import com.softserve.bookstoreapi.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
public class BookController {
    private final BookService bookService;
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/api/book")
    public ResponseEntity<Book> createBook(@RequestBody BookDTO request) {
        Book created = bookService.createBook(request);
        return ResponseEntity.ok(created);
    }
    @GetMapping("/api/book")
    public Page<BookDTO> getBooks(
            @RequestParam(required = false) String genreName,
            @RequestParam(required = false) String title,
            Pageable pageable
    ) {
        return bookService.getBooks(genreName,title ,pageable);
    }
}
