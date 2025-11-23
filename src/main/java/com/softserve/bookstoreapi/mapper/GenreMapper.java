package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.DTO.GenreDTO;
import com.softserve.bookstoreapi.model.Genre;
import org.springframework.stereotype.Component;

@Component
public class GenreMapper {
    public static GenreDTO toDto(Genre genre) {
        return new GenreDTO(genre.getName(), genre.getDescription());
    }

    public static Genre toEntity(GenreDTO dto) {
        Genre genre = new Genre();
        genre.setName(dto.name());
        genre.setDescription(dto.description());
        return genre;
    }
}
