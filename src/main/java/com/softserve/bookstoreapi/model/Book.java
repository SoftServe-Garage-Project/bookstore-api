package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.generaEntities.SoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "book", indexes = {
        @Index(name = "idx_books_title_active", columnList = "title, is_active"),
        @Index(name = "idx_books_genre_active", columnList = "genre_id, is_active")})
public class Book extends SoftDeletableEntity {

    @NotBlank(message = "{validation.book.title.notblank}")
    @Size(min = 2, max = 255, message = "{validation.book.title.size}")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "{validation.book.genre.notnull}")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @NotNull(message = "{validation.book.agegroup.notnull}")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "age_group_id", nullable = false)
    private AgeGroup ageGroup;

    @NotNull(message = "{validation.book.publishedyear.notnull}")
    @Min(value = 1000, message = "{validation.book.publishedyear.min}")
    @Column(nullable = false, name = "published_year")
    private Integer publishedYear;

    @NotNull(message = "{validation.book.language.notnull}")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<Author> authors = new ArrayList<>();

    @NotNull(message = "{validation.book.price.notnull}")
    @DecimalMin(value = "0.0", message = "{validation.book.price.min}")
    @DecimalMax(value = "999999.99", message = "{validation.book.price.max}")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "{validation.book.stockquantity.notnull}")
    @Min(value = 0, message = "{validation.book.stockquantity.min}")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @DecimalMin(value = "0.0", message = "{validation.book.discount.min}")
    @DecimalMax(value = "100.0", message = "{validation.book.discount.max}")
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Min(value = 1, message = "{validation.book.pagecount.min}")
    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
}
