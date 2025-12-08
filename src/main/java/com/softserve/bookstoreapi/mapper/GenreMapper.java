package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.DTO.GenreDTO;
import com.softserve.bookstoreapi.model.Genre;
import org.springframework.stereotype.Component;

@Component
public class GenreMapper {
    public GenreDTO toDto(Genre genre) {
        return new GenreDTO(genre.getName(), genre.getDescription());
    }

    public Genre toEntity(GenreDTO dto) {
        return new Genre (
                dto.name(),
                dto.description()
        );
    }
}
