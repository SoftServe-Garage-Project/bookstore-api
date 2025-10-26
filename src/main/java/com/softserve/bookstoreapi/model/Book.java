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
@Table(
        name = "book",
        indexes = {
                @Index(name = "idx_book_title_active", columnList = "title, active"),
                @Index(name = "idx_book_genre_active", columnList = "genre_id, active"),
                @Index(name = "idx_book_price_range", columnList = "price"),
                @Index(name = "idx_book_published_year_active", columnList = "published_year, active"),
                @Index(name = "idx_book_language_age_active", columnList = "language_id, age_group_id, active"),
                @Index(name = "idx_book_stock_discount_active", columnList = "stock_quantity, discount_percentage, active"),
                @Index(name = "idx_book_page_count", columnList = "page_count")
        }
)
public class Book extends SoftDeletableEntity {

    @NotBlank(message = "Назва книги обов'язкова")
    @Size(min = 2, max = 255, message = "Назва повинна містити від 2 до 255 символів")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @NotNull(message = "Вікова група обов'язкова")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "age_group_id", nullable = false)
    private AgeGroup ageGroup;

    @NotNull(message = "Рік видання обов'язковий")
    @Min(value = 1000, message = "Рік видання повинен бути не менше 1000")
    @Column(nullable = false, name = "published_year")
    private Integer publishedYear;

    @NotNull(message = "Мова обов'язкова")
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

    @NotNull(message = "Ціна обов'язкова")
    @DecimalMin(value = "0.0", message = "Ціна не може бути від'ємною")
    @DecimalMax(value = "999999.99", message = "Ціна занадто велика")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Кількість на складі обов'язкова")
    @Min(value = 0, message = "Кількість не може бути від'ємною")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @DecimalMin(value = "0.0", message = "Знижка не може бути від'ємною")
    @DecimalMax(value = "100.0", message = "Знижка не може перевищувати 100%")
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
}
