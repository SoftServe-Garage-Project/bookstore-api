package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.generaEntities.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "review",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}),
        indexes = {
                @Index(name = "idx_review_book_created_desc", columnList = "book_id, created_at DESC"),
                @Index(name = "idx_review_user_created_desc", columnList = "user_id, created_at DESC"),
                @Index(name = "idx_review_book_rating", columnList = "book_id, rating"),
                @Index(name = "idx_review_rating_created", columnList = "rating, created_at")
        }
)
public class Review extends AuditableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull(message = "Рейтинг обов'язковий")
    @Min(value = 1, message = "Рейтинг не може бути меншим за 1")
    @Max(value = 5, message = "Рейтинг не може бути більшим за 5")
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;
}
