package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.GenreDTO;
import com.softserve.bookstoreapi.mapper.GenreMapper;
import com.softserve.bookstoreapi.model.Genre;
import com.softserve.bookstoreapi.repository.BookRepository;
import com.softserve.bookstoreapi.repository.GenreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class GenreService {
    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;
    private final BookRepository bookRepository;

    public GenreService(GenreRepository genreRepository, GenreMapper genreMapper, BookRepository bookRepository) {
        this.genreRepository = genreRepository;
        this.genreMapper = genreMapper;
        this.bookRepository = bookRepository;
    }

    public GenreDTO addGenre(GenreDTO genreDTO){
        Genre newGenre = new Genre();
        newGenre.setName(genreDTO.name());
        newGenre.setDescription(genreDTO.description());

        Genre savedGenre = genreRepository.save(newGenre);
        return genreMapper.toDto(savedGenre);
    }
    @Transactional
    public Page<GenreDTO> getAllGenres(Pageable pageable) {
        Page<Genre> genres = genreRepository.findAll(pageable);
        return genres.map(genreMapper::toDto);
    }
    @Transactional
    public void deleteGenre(Long id) {
        if (!genreRepository.existsById(id)) {
            throw new RuntimeException("Genre not found with id: " + id);
        }

        if (bookRepository.existsByGenreId(id)) {
            throw new RuntimeException("Cannot delete genre. It is used by existing books.");
        }

        genreRepository.deleteById(id);
    }
}