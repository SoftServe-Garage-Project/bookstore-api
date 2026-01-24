package com.softserve.bookstoreapi.service;

import com.softserve.bookstoreapi.dto.PromoCodeDTO;
import com.softserve.bookstoreapi.dto.PromoCodeValidationRequestDTO;
import com.softserve.bookstoreapi.dto.PromoCodeValidationResponseDTO;
import com.softserve.bookstoreapi.exception.InvalidPromoCodeException;
import com.softserve.bookstoreapi.exception.PromoCodeNotFoundException;
import com.softserve.bookstoreapi.mapper.PromoCodeMapper;
import com.softserve.bookstoreapi.model.PromoCode;
import com.softserve.bookstoreapi.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeService {

    private static final String PROMO_CODE_NOT_FOUND_MSG = "Promo code not found with ID: ";

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;

    @Transactional
    public PromoCodeDTO createPromoCode(PromoCodeDTO promoCodeDTO) {
        log.info("Creating new promo code: {}", promoCodeDTO.code());

        if (promoCodeRepository.existsByCode(promoCodeDTO.code())) {
            throw new InvalidPromoCodeException("Promo code with code '" + promoCodeDTO.code() + "' already exists");
        }

        PromoCode promoCode = promoCodeMapper.toEntity(promoCodeDTO);
        PromoCode savedPromoCode = promoCodeRepository.save(promoCode);

        log.info("Promo code created successfully with ID: {}", savedPromoCode.getId());
        return promoCodeMapper.toDto(savedPromoCode);
    }

    public Page<PromoCodeDTO> getAllPromoCodes(Pageable pageable) {
        log.debug("Fetching all promo codes, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<PromoCode> promoCodes = promoCodeRepository.findAll(pageable);
        return promoCodes.map(promoCodeMapper::toDto);
    }

    public PromoCodeDTO getPromoCodeById(Long id) {
        log.debug("Fetching promo code by ID: {}", id);
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new PromoCodeNotFoundException(PROMO_CODE_NOT_FOUND_MSG + id));
        return promoCodeMapper.toDto(promoCode);
    }

    @Transactional
    public PromoCodeDTO updatePromoCode(Long id, PromoCodeDTO promoCodeDTO) {
        log.info("Updating promo code with ID: {}", id);

        PromoCode existingPromoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new PromoCodeNotFoundException(PROMO_CODE_NOT_FOUND_MSG + id));

        if (!existingPromoCode.getCode().equals(promoCodeDTO.code()) &&
                promoCodeRepository.existsByCode(promoCodeDTO.code())) {
            throw new InvalidPromoCodeException("Promo code with code '" + promoCodeDTO.code() + "' already exists");
        }

        promoCodeMapper.updateEntityFromDto(promoCodeDTO, existingPromoCode);
        PromoCode updatedPromoCode = promoCodeRepository.save(existingPromoCode);

        log.info("Promo code updated successfully: {}", updatedPromoCode.getId());
        return promoCodeMapper.toDto(updatedPromoCode);
    }

    @Transactional
    public void deletePromoCode(Long id) {
        log.info("Deleting promo code with ID: {}", id);

        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new PromoCodeNotFoundException("Promo code not found with ID: " + id));

        promoCode.setIsActive(false);
        promoCodeRepository.save(promoCode);

        log.info("Promo code soft deleted successfully: {}", id);
    }

    @Transactional
    public void activatePromoCode(Long id) {
        log.info("Activating promo code with ID: {}", id);

        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new PromoCodeNotFoundException("Promo code not found with ID: " + id));

        promoCode.setIsActive(true);
        promoCodeRepository.save(promoCode);

        log.info("Promo code activated successfully: {}", id);
    }

    @Transactional
    public void deactivatePromoCode(Long id) {
        log.info("Deactivating promo code with ID: {}", id);

        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new PromoCodeNotFoundException("Promo code not found with ID: " + id));

        promoCode.setIsActive(false);
        promoCodeRepository.save(promoCode);

        log.info("Promo code deactivated successfully: {}", id);
    }

    public PromoCodeValidationResponseDTO validatePromoCode(PromoCodeValidationRequestDTO request) {
        log.debug("Validating promo code: {}", request.code());

        PromoCode promoCode = promoCodeRepository.findByCodeAndIsActiveTrue(request.code())
                .orElse(null);

        if (promoCode == null) {
            return new PromoCodeValidationResponseDTO(
                    false,
                    "Promo code not found or inactive",
                    null,
                    null,
                    request.orderAmount()
            );
        }

        LocalDateTime nowUtc = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);

        if (promoCode.getValidFrom().isAfter(nowUtc)) {
            return new PromoCodeValidationResponseDTO(
                    false,
                    "Promo code is not yet valid",
                    null,
                    null,
                    request.orderAmount()
            );
        }

        if (promoCode.getValidTo() != null && promoCode.getValidTo().isBefore(nowUtc)) {
            return new PromoCodeValidationResponseDTO(
                    false,
                    "Promo code has expired",
                    null,
                    null,
                    request.orderAmount()
            );
        }

        if (promoCode.getMaxUses() != null && promoCode.getCurrentUses() >= promoCode.getMaxUses()) {
            return new PromoCodeValidationResponseDTO(
                    false,
                    "Promo code usage limit reached",
                    null,
                    null,
                    request.orderAmount()
            );
        }

        if (request.orderAmount().compareTo(promoCode.getMinOrderAmount()) < 0) {
            return new PromoCodeValidationResponseDTO(
                    false,
                    "Order amount does not meet minimum requirement of " + promoCode.getMinOrderAmount(),
                    null,
                    null,
                    request.orderAmount()
            );
        }

        BigDecimal discountAmount = request.orderAmount()
                .multiply(promoCode.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalAmount = request.orderAmount().subtract(discountAmount);

        log.info("Promo code validated successfully: {}", request.code());

        return new PromoCodeValidationResponseDTO(
                true,
                "Promo code is valid",
                promoCode.getDiscountPercentage(),
                discountAmount,
                finalAmount
        );
    }

    @Transactional
    public void incrementUsage(String code) {
        log.debug("Incrementing usage for promo code: {}", code);

        PromoCode promoCode = promoCodeRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new PromoCodeNotFoundException("Promo code not found: " + code));

        promoCode.setCurrentUses(promoCode.getCurrentUses() + 1);
        promoCodeRepository.save(promoCode);

        log.info("Promo code usage incremented: {} (current uses: {})", code, promoCode.getCurrentUses());
    }
}
