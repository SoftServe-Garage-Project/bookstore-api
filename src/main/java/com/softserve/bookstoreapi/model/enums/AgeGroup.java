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
        name = "age_group",
        indexes = {
                @Index(name = "idx_age_group_name", columnList = "name"),
                @Index(name = "idx_age_group_active", columnList = "active")
        }
)
public class AgeGroup extends AuditableEntity {

    @NotBlank(message = "Назва вікової групи обов'язкова")
    @Size(min = 2, max = 50, message = "Назва повинна містити від 2 до 50 символів")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(nullable = false)
    private Boolean active = true;
}
