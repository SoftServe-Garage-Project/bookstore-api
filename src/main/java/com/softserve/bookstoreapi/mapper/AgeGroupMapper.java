package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.dto.AgeGroupDTO;
import com.softserve.bookstoreapi.model.AgeGroup;
import org.springframework.stereotype.Component;

@Component
public class AgeGroupMapper {

    public AgeGroup toEntity(AgeGroupDTO dto) {
        AgeGroup ageGroup = new AgeGroup();
        ageGroup.setId(dto.id());
        ageGroup.setName(dto.name());
        ageGroup.setDescription(dto.description());
        ageGroup.setMinAge(dto.minAge());
        ageGroup.setMaxAge(dto.maxAge());
        return ageGroup;
    }

    public AgeGroupDTO toDto(AgeGroup entity) {
        return new AgeGroupDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getMinAge(),
                entity.getMaxAge()
        );
    }
}