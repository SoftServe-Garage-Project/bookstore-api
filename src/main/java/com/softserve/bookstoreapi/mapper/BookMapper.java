package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.dto.AuthorDTO;
import com.softserve.bookstoreapi.dto.BookDTO;

import com.softserve.bookstoreapi.model.Book;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
@Component
public class BookMapper {

    public BookDTO toDto(Book book) {
        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getDescription(),
                book.getGenre().getName(),
                book.getAgeGroup().getName(),
                book.getPublishedYear(),
                book.getLanguage().getName(),
                book.getAuthors().stream()
                        .map(a -> new AuthorDTO(a.getFirstName(), a.getLastName()))
                        .collect(Collectors.toList()),
                book.getPrice(),
                book.getStockQuantity(),
                book.getDiscountPercentage(),
                book.getPageCount(),
                book.getCoverImageUrl()
        );
    }
}