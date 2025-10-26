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
        name = "language",
        indexes = {
                @Index(name = "idx_language_code", columnList = "code"),
                @Index(name = "idx_language_active", columnList = "active")
        }
)
public class Language extends SoftDeletableEntity {

    @NotBlank(message = "Код мови обов'язковий")
    @Size(min = 2, max = 10, message = "Код мови повинен містити від 2 до 10 символів")
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @NotBlank(message = "Назва мови обов'язкова")
    @Size(min = 2, max = 100, message = "Назва мови повинна містити від 2 до 100 символів")
    @Column(nullable = false, length = 100)
    private String name;
}
