package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.DTO.LanguageDTO;
import com.softserve.bookstoreapi.mapper.LanguageMapper;
import com.softserve.bookstoreapi.model.Language;
import com.softserve.bookstoreapi.repository.LanguageRepository;
import com.softserve.bookstoreapi.service.LanguageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LanguageController {
    private final LanguageService languageService;
    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @PostMapping("/api/languages")
    public ResponseEntity<LanguageDTO> saveLanguage(@Valid @RequestBody LanguageDTO languageDTO) {
        Language newLanguage = languageService.saveLanguage(languageDTO);
        return ResponseEntity.ok(LanguageMapper.toDto(newLanguage));
    }
}
