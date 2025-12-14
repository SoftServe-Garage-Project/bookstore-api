package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.LanguageDTO;
import com.softserve.bookstoreapi.model.Language;
import com.softserve.bookstoreapi.repository.LanguageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LanguageService {

    private final LanguageRepository languageRepository;
    public LanguageService(LanguageRepository languageRepository)
    {
        this.languageRepository = languageRepository;
    }

    public Language saveLanguage(LanguageDTO languageDTO) {
        Language newLanguage = new Language();
        newLanguage.setCode(languageDTO.code());
        newLanguage.setName(languageDTO.name());
        return languageRepository.save(newLanguage);
    }
}
