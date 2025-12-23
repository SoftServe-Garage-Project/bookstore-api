package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.dto.LanguageDTO;
import com.softserve.bookstoreapi.model.Language;
import org.springframework.stereotype.Component;

@Component
public class LanguageMapper {
    public LanguageDTO toDto(Language language) {
        return new LanguageDTO(language.getCode(), language.getName());
    }

    public Language toEntity(LanguageDTO dto) {
        Language language = new Language();
        language.setCode(dto.code());
        language.setName(dto.name());
        return language;
    }
}
