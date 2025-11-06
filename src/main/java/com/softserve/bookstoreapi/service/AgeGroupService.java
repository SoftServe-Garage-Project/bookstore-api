package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.model.AgeGroup;
import com.softserve.bookstoreapi.repository.AgeGroupRepository;
import com.softserve.bookstoreapi.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AgeGroupService {
    private final AgeGroupRepository ageGroupRepository;

    public AgeGroupService(AgeGroupRepository ageGroupRepository) {
        this.ageGroupRepository = ageGroupRepository;
    }


    public Optional<AgeGroup> findByName(String name) {
        return ageGroupRepository.findByNameIgnoreCase(name);
    }

    public AgeGroup save(AgeGroup ageGroup) {
        return ageGroupRepository.save(ageGroup);
    }




}
