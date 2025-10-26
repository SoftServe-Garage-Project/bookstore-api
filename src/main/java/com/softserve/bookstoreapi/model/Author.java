package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.generaEntities.SoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "author",
        indexes = {
                @Index(name = "idx_author_name_composite", columnList = "last_name, first_name"),
                @Index(name = "idx_author_country_active", columnList = "country, active"),
                @Index(name = "idx_author_active", columnList = "active")
        }
)
public class Author extends SoftDeletableEntity {

    @NotBlank(message = "Ім'я автора обов'язкове")
    @Size(min = 2, max = 100, message = "Ім'я повинно містити від 2 до 100 символів")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Прізвище автора обов'язкове")
    @Size(min = 2, max = 100, message = "Прізвище повинно містити від 2 до 100 символів")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(length = 100)
    private String country;
}
