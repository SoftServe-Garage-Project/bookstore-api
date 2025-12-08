package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.DTO.GenreDTO;
import com.softserve.bookstoreapi.mapper.GenreMapper;
import com.softserve.bookstoreapi.mapper.LanguageMapper;
import com.softserve.bookstoreapi.model.Genre;
import com.softserve.bookstoreapi.model.Language;
import com.softserve.bookstoreapi.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenreController {
    private final GenreService genreService;
    private final GenreMapper genreMapper;
    public GenreController(GenreService genreService, GenreMapper genreMapper) {
        this.genreService = genreService;
        this.genreMapper = genreMapper;
    }

    @PostMapping("/api/genres")
    public ResponseEntity<GenreDTO> createGenre(@Valid @RequestBody GenreDTO genreDTO) {
        GenreDTO createdGenre = genreService.addGenre(genreDTO);
        return ResponseEntity.ok(createdGenre);
    }
}
