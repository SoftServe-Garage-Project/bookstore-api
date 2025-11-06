package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.DTO.AgeGroupDto;
import com.softserve.bookstoreapi.model.AgeGroup;
import org.springframework.stereotype.Component;

@Component
public class AgeGroupMapper {

    public AgeGroup toEntity(AgeGroupDto dto) {
        AgeGroup ageGroup = new AgeGroup();
        ageGroup.setName(dto.name());
        ageGroup.setDescription(dto.description());
        ageGroup.setMinAge(dto.minAge());
        ageGroup.setMaxAge(dto.maxAge());
        return ageGroup;
    }

    public AgeGroupDto toDto(AgeGroup entity) {
        return new AgeGroupDto(
                entity.getName(),
                entity.getDescription(),
                entity.getMinAge(),
                entity.getMaxAge()
        );
    }
}
