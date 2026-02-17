package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.AgeGroupDTO;
import com.softserve.bookstoreapi.mapper.AgeGroupMapper;
import com.softserve.bookstoreapi.model.AgeGroup;
import com.softserve.bookstoreapi.repository.AgeGroupRepository;
import com.softserve.bookstoreapi.repository.BookRepository;
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
    private final BookRepository bookRepository;

    public AgeGroupService(AgeGroupRepository ageGroupRepository, AgeGroupMapper ageGroupMapper, BookRepository bookRepository) {
        this.ageGroupRepository = ageGroupRepository;
        this.ageGroupMapper = ageGroupMapper;
        this.bookRepository = bookRepository;
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
    @Transactional
    public void deleteAgeGroup(Long id) {
        if (!ageGroupRepository.existsById(id)) {
            throw new RuntimeException("Age group not found with id: " + id);
        }

        if (bookRepository.existsByAgeGroupId(id)) {
            throw new RuntimeException("Cannot delete age group. It is used by existing books.");
        }

        ageGroupRepository.deleteById(id);
    }
}
