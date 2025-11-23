package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.DTO.LanguageDTO;
import com.softserve.bookstoreapi.model.Language;
import org.springframework.stereotype.Component;

@Component
public class LanguageMapper {
    public static LanguageDTO toDto(Language language) {
        return new LanguageDTO(language.getCode(), language.getName());
    }

    public static Language toEntity(LanguageDTO dto) {
        Language language = new Language();
        language.setCode(dto.code());
        language.setName(dto.name());
        return language;
    }
}
