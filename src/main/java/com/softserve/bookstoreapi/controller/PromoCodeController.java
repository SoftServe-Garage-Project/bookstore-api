package com.softserve.bookstoreapi.controller;

import com.softserve.bookstoreapi.dto.PromoCodeDTO;
import com.softserve.bookstoreapi.dto.PromoCodeValidationRequestDTO;
import com.softserve.bookstoreapi.dto.PromoCodeValidationResponseDTO;
import com.softserve.bookstoreapi.service.PromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @PostMapping
    public ResponseEntity<PromoCodeDTO> createPromoCode(@Valid @RequestBody PromoCodeDTO promoCodeDTO) {
        PromoCodeDTO created = promoCodeService.createPromoCode(promoCodeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<PromoCodeDTO>> getAllPromoCodes(
            @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<PromoCodeDTO> promoCodes = promoCodeService.getAllPromoCodes(pageable);
        return ResponseEntity.ok(promoCodes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeDTO> getPromoCodeById(@PathVariable Long id) {
        PromoCodeDTO promoCode = promoCodeService.getPromoCodeById(id);
        return ResponseEntity.ok(promoCode);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeDTO> updatePromoCode(
            @PathVariable Long id,
            @Valid @RequestBody PromoCodeDTO promoCodeDTO
    ) {
        PromoCodeDTO updated = promoCodeService.updatePromoCode(id, promoCodeDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromoCode(@PathVariable Long id) {
        promoCodeService.deletePromoCode(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activatePromoCode(@PathVariable Long id) {
        promoCodeService.activatePromoCode(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePromoCode(@PathVariable Long id) {
        promoCodeService.deactivatePromoCode(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<PromoCodeValidationResponseDTO> validatePromoCode(
            @Valid @RequestBody PromoCodeValidationRequestDTO request
    ) {
        PromoCodeValidationResponseDTO response = promoCodeService.validatePromoCode(request);
        return ResponseEntity.ok(response);
    }
}
