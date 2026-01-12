package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.AgeGroupDTO;
import com.softserve.bookstoreapi.mapper.AgeGroupMapper;
import com.softserve.bookstoreapi.model.AgeGroup;
import com.softserve.bookstoreapi.repository.AgeGroupRepository;
import com.softserve.bookstoreapi.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AgeGroupService {
    private final AgeGroupRepository ageGroupRepository;
    private final AgeGroupMapper ageGroupMapper;

    public AgeGroupService(AgeGroupRepository ageGroupRepository, AgeGroupMapper ageGroupMapper) {
        this.ageGroupRepository = ageGroupRepository;
        this.ageGroupMapper = ageGroupMapper;
    }


    public Optional<AgeGroup> findByName(String name) {
        return ageGroupRepository.findByNameIgnoreCase(name);
    }

    public AgeGroup save(AgeGroup ageGroup) {
        return ageGroupRepository.save(ageGroup);
    }

    @Transactional
    public Page<AgeGroupDTO> getAllAgeGroups(Pageable pageable) {
        Page<AgeGroup> page = ageGroupRepository.findAll(pageable);
        return page.map(ageGroupMapper::toDto);
    }
}
