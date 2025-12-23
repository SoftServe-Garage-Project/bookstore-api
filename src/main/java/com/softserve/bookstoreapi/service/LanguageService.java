package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.LanguageDTO;
import com.softserve.bookstoreapi.mapper.LanguageMapper;
import com.softserve.bookstoreapi.model.Language;
import com.softserve.bookstoreapi.repository.LanguageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LanguageService {

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;

    public LanguageService(LanguageRepository languageRepository, LanguageMapper languageMapper)
    {
        this.languageRepository = languageRepository;
        this.languageMapper = languageMapper;
    }

    public Language saveLanguage(LanguageDTO languageDTO) {
        Language newLanguage = new Language();
        newLanguage.setCode(languageDTO.code());
        newLanguage.setName(languageDTO.name());
        return languageRepository.save(newLanguage);
    }

    @Transactional
    public Page<LanguageDTO> getAllLanguages(Pageable pageable) {
        Page<Language> page = languageRepository.findAll(pageable);
        return page.map(languageMapper::toDto);
    }
}
