package com.softserve.bookstoreapi.model;

import com.softserve.bookstoreapi.model.generaEntities.BaseEntity;
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
@Table(name = "languages")
public class Language extends BaseEntity {

    @NotBlank(message = "{validation.language.code.notblank}")
    @Size(min = 2, max = 10, message = "{validation.language.code.size}")
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @NotBlank(message = "{validation.language.name.notblank}")
    @Size(min = 2, max = 100, message = "{validation.language.name.size}")
    @Column(nullable = false, length = 100)
    private String name;
}
