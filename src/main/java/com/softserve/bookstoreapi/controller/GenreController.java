package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.DTO.GenreDTO;
import com.softserve.bookstoreapi.model.Genre;
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

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }
    @PostMapping("/api/genres")
    public ResponseEntity<Genre> createGenre (@Valid @RequestBody GenreDTO genreDTO){
        Genre createdGenre = genreService.addGenre(genreDTO);
        return ResponseEntity.ok().body(createdGenre);
    }
}
