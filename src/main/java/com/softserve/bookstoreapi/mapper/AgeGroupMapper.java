package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.DTO.AgeGroupDTO;
import com.softserve.bookstoreapi.model.AgeGroup;
import org.springframework.stereotype.Component;

@Component
public class AgeGroupMapper {

    public AgeGroup toEntity(AgeGroupDTO dto) {
        return new AgeGroup(
                dto.name(),
                dto.description(),
                dto.minAge(),
                dto.maxAge()
        );
    }

    public AgeGroupDTO toDto(AgeGroup entity) {
        return new AgeGroupDTO(
                entity.getName(),
                entity.getDescription(),
                entity.getMinAge(),
                entity.getMaxAge()
        );
    }
}
