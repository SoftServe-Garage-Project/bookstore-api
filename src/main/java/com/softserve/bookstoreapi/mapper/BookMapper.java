package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.DTO.AuthorDTO;
import com.softserve.bookstoreapi.DTO.BookDTO;

import com.softserve.bookstoreapi.model.Book;

import java.util.stream.Collectors;

public class BookMapper {

    public static BookDTO toDto(Book book) {
        return new BookDTO(
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