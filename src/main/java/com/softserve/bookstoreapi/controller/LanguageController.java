package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.LanguageDTO;
import com.softserve.bookstoreapi.mapper.LanguageMapper;
import com.softserve.bookstoreapi.model.Language;
import com.softserve.bookstoreapi.service.LanguageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LanguageController {
    private final LanguageService languageService;
    private final LanguageMapper languageMapper;

    public LanguageController(LanguageService languageService, LanguageMapper languageMapper) {
        this.languageService = languageService;
        this.languageMapper = languageMapper;
    }

    @PostMapping("/api/languages")
    public ResponseEntity<LanguageDTO> saveLanguage(@Valid @RequestBody LanguageDTO languageDTO) {
        Language newLanguage = languageService.saveLanguage(languageDTO);
        return ResponseEntity.ok(languageMapper.toDto(newLanguage));
    }

    @GetMapping("/api/languages")
    public ResponseEntity<Page<LanguageDTO>> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        Page<LanguageDTO> response = languageService.getAllLanguages(pageable);
        return ResponseEntity.ok(response);
    }
}
