package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.DTO.GenreDTO;
import com.softserve.bookstoreapi.model.Genre;
import com.softserve.bookstoreapi.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Genre addGenre(GenreDTO genre){
        Genre newGenre = new Genre();
        newGenre.setName(genre.name());
        newGenre.setDescription(genre.description());
        return genreRepository.save(newGenre);
    }
}

