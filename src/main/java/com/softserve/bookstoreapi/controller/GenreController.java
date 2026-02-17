package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.GenreDTO;
import com.softserve.bookstoreapi.mapper.GenreMapper;
import com.softserve.bookstoreapi.service.GenreService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/api/genres")
    public ResponseEntity<Page<GenreDTO>> getAllGenres(
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable
    ) {
        Page<GenreDTO> page = genreService.getAllGenres(pageable);
        return ResponseEntity.ok(page);
    }
    @DeleteMapping("/api/genres/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}