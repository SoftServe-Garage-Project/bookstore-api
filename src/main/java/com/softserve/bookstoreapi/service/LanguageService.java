package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.LanguageDTO;
import com.softserve.bookstoreapi.mapper.LanguageMapper;
import com.softserve.bookstoreapi.model.Language;
import com.softserve.bookstoreapi.repository.BookRepository;
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
    private final BookRepository bookRepository;

    public LanguageService(LanguageRepository languageRepository, LanguageMapper languageMapper, BookRepository bookRepository)
    {
        this.languageRepository = languageRepository;
        this.languageMapper = languageMapper;
        this.bookRepository = bookRepository;
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

    @Transactional
    public void deleteLanguage(Long id) {
        if (!languageRepository.existsById(id)) {
            throw new RuntimeException("Language not found with id: " + id);
        }

        if (bookRepository.existsByLanguageId(id)) {
            throw new RuntimeException("Cannot delete language. It is used by existing books.");
        }

        languageRepository.deleteById(id);
    }
}
