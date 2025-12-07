package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.DTO.AgeGroupDTO;
import com.softserve.bookstoreapi.mapper.AgeGroupMapper;
import com.softserve.bookstoreapi.model.AgeGroup;
import com.softserve.bookstoreapi.service.AgeGroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AgeGroupController {

    private final AgeGroupService ageGroupService;
    private final AgeGroupMapper ageGroupMapper;

    public AgeGroupController(AgeGroupService ageGroupService, AgeGroupMapper ageGroupMapper) {
        this.ageGroupService = ageGroupService;
        this.ageGroupMapper = ageGroupMapper;
    }

    @PostMapping("/api/ageGroups")
    public ResponseEntity<AgeGroupDTO> create(@Valid @RequestBody AgeGroupDTO dto) {
        AgeGroup saved = ageGroupService.save(ageGroupMapper.toEntity(dto));
        return ResponseEntity.ok(ageGroupMapper.toDto(saved));
    }
}
