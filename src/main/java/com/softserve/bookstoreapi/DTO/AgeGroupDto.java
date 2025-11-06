package com.softserve.bookstoreapi.DTO;

import jakarta.validation.constraints.*;

public record AgeGroupDto(
        @NotBlank(message = "{validation.agegroup.name.notblank}")
        @Size(min = 2, max = 50, message = "{validation.agegroup.name.size}")
        String name,

        String description,

        @NotNull(message = "{validation.agegroup.minage.notnull}")
        @Min(value = 0, message = "{validation.agegroup.minage.min}")
        Integer minAge,

        @NotNull(message = "{validation.agegroup.maxage.notnull}")
        @Min(value = 1, message = "{validation.agegroup.maxage.min}")
        @Max(value = 120, message = "{validation.agegroup.maxage.max}")
        Integer maxAge
) {}
