package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.generaEntities.SoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "age_group")
public class AgeGroup extends SoftDeletableEntity {

    @NotBlank(message = "{validation.agegroup.name.notblank}")
    @Size(min = 2, max = 50, message = "{validation.agegroup.name.size}")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "{validation.agegroup.minage.notnull}")
    @Min(value = 0, message = "{validation.agegroup.minage.min}")
    @Column(name = "min_age")
    private Integer minAge;

    @NotNull(message = "{validation.agegroup.maxage.notnull}")
    @Min(value = 1, message = "{validation.agegroup.maxage.min}")
    @Max(value = 120, message = "{validation.agegroup.maxage.max}")
    @Column(name = "max_age")
    private Integer maxAge;
}
