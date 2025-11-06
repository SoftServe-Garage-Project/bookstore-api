package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.DTO.LanguageDTO;
import com.softserve.bookstoreapi.model.Genre;
import com.softserve.bookstoreapi.model.Language;
import com.softserve.bookstoreapi.repository.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LanguageService {

    private final LanguageRepository languageRepository;
    public LanguageService(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }
    public Language saveLanguage(LanguageDTO languageDTO) {
       if(languageRepository.findByCodeIgnoreCase(languageDTO.getCode()).isPresent()){
           throw new IllegalArgumentException("Age group with this name already exists.");
       }
        Language newLanguage = new Language();
        newLanguage.setCode(languageDTO.getCode());
        newLanguage.setName(languageDTO.getCode());

        return languageRepository.save(newLanguage);
    }
}
