package com.softserve.bookstoreapi.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public record LanguageDTO(
        Long id,
        @NotBlank(message = "{validation.language.code.notblank}")
        String code,

        @NotBlank(message = "{validation.language.code.notblank}")
        String name
){}