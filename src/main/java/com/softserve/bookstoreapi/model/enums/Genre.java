package com.softserve.bookstoreapi.model.enums;

import com.softserve.bookstoreapi.model.generaEntities.AuditableEntity;
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
        name = "genre",
        indexes = {
                @Index(name = "idx_genre_name", columnList = "name"),
                @Index(name = "idx_genre_active", columnList = "active")
        }
)
public class Genre extends AuditableEntity {

    @NotBlank(message = "Назва жанру обов'язкова")
    @Size(min = 2, max = 100, message = "Назва жанру повинна містити від 2 до 100 символів")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean active = true;
}
