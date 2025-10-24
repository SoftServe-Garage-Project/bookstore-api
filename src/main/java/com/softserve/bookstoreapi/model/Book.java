package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.enums.AgeGroup;
import com.softserve.bookstoreapi.model.enums.Genre;
import com.softserve.bookstoreapi.model.enums.Language;
import com.softserve.bookstoreapi.model.generaEntities.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book extends AuditableEntity {

    @NotBlank(message = "Назва книги обов'язкова")
    @Size(min = 2, max = 255, message = "Назва повинна містити від 2 до 255 символів")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Автор обов'язковий")
    @Size(min = 2, max = 255, message = "Ім'я автора повинно містити від 2 до 255 символів")
    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Genre genre;

    @NotNull(message = "Вікова група обов'язкова")
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 50)
    private AgeGroup ageGroup;

    @NotNull(message = "Рік видання обов'язковий")
    @Min(value = 1000, message = "Рік видання повинен бути не менше 1000")
    @Max(value = 2025, message = "Рік видання не може бути більше поточного року")
    @Column(nullable = false, name = "published_year")
    private Integer publishedYear;

    @NotNull(message = "Мова обов'язкова")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Language language;

    @NotNull(message = "Ціна обов'язкова")
    @DecimalMin(value = "0.0", inclusive = true, message = "Ціна не може бути від'ємною")
    @DecimalMax(value = "999999.99", message = "Ціна занадто велика")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Кількість на складі обов'язкова")
    @Min(value = 0, message = "Кількість не може бути від'ємною")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Знижка не може бути від'ємною")
    @DecimalMax(value = "100.0", message = "Знижка не може перевищувати 100%")
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Review> reviews;
}