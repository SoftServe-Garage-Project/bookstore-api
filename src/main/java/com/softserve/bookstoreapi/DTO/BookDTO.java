package com.softserve.bookstoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private String title;
    private String description;
    private String genre;
    private String ageGroup;
    private Integer publishedYear;
    private String languageCode;
    private List<AuthorDTO> authors;
    private BigDecimal price;
    private Integer stockQuantity;
    private BigDecimal discountPercentage;
    private Integer pageCount;
    private String coverImageUrl;
}

