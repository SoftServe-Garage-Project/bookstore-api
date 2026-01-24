package service;

import com.softserve.bookstoreapi.dto.PromoCodeDTO;
import com.softserve.bookstoreapi.dto.PromoCodeValidationRequestDTO;
import com.softserve.bookstoreapi.dto.PromoCodeValidationResponseDTO;
import com.softserve.bookstoreapi.exception.InvalidPromoCodeException;
import com.softserve.bookstoreapi.exception.PromoCodeNotFoundException;
import com.softserve.bookstoreapi.mapper.PromoCodeMapper;
import com.softserve.bookstoreapi.model.PromoCode;
import com.softserve.bookstoreapi.repository.PromoCodeRepository;
import com.softserve.bookstoreapi.service.PromoCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromoCodeService Tests")
class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @Mock
    private PromoCodeMapper promoCodeMapper;

    @InjectMocks
    private PromoCodeService promoCodeService;

    private PromoCode promoCodeEntity;
    private PromoCodeDTO promoCodeDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);

        promoCodeEntity = new PromoCode();
        promoCodeEntity.setId(1L);
        promoCodeEntity.setCode("SUMMER2024");
        promoCodeEntity.setDiscountPercentage(new BigDecimal("15.00"));
        promoCodeEntity.setDescription("Summer sale 2024");
        promoCodeEntity.setValidFrom(now.minusDays(1)); // Started yesterday
        promoCodeEntity.setValidTo(now.plusMonths(3)); // Expires in 3 months
        promoCodeEntity.setMaxUses(100);
        promoCodeEntity.setCurrentUses(0);
        promoCodeEntity.setMinOrderAmount(new BigDecimal("50.00"));
        promoCodeEntity.setIsActive(true);

        promoCodeDto = new PromoCodeDTO(
                1L,
                "SUMMER2024",
                new BigDecimal("15.00"),
                "Summer sale 2024",
                now.minusDays(1),
                now.plusMonths(3),
                100,
                0,
                new BigDecimal("50.00"),
                true
        );
    }

    @Test
    @DisplayName("createPromoCode: Should create promo code successfully")
    void createPromoCode_Success() {
        when(promoCodeRepository.existsByCode("SUMMER2024")).thenReturn(false);
        when(promoCodeMapper.toEntity(any(PromoCodeDTO.class))).thenReturn(promoCodeEntity);
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCodeEntity);
        when(promoCodeMapper.toDto(any(PromoCode.class))).thenReturn(promoCodeDto);

        PromoCodeDTO result = promoCodeService.createPromoCode(promoCodeDto);

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("SUMMER2024");
        assertThat(result.discountPercentage()).isEqualByComparingTo(new BigDecimal("15.00"));

        verify(promoCodeRepository, times(1)).existsByCode("SUMMER2024");
        verify(promoCodeRepository, times(1)).save(any(PromoCode.class));
    }

    @Test
    @DisplayName("createPromoCode: Should throw exception when code already exists")
    void createPromoCode_DuplicateCode() {
        when(promoCodeRepository.existsByCode("SUMMER2024")).thenReturn(true);

        assertThatThrownBy(() -> promoCodeService.createPromoCode(promoCodeDto))
                .isInstanceOf(InvalidPromoCodeException.class)
                .hasMessageContaining("already exists");

        verify(promoCodeRepository, times(1)).existsByCode("SUMMER2024");
        verify(promoCodeRepository, never()).save(any(PromoCode.class));
    }

    @Test
    @DisplayName("getAllPromoCodes: Should return page of promo codes")
    void getAllPromoCodes_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PromoCode> promoCodePage = new PageImpl<>(Collections.singletonList(promoCodeEntity));

        when(promoCodeRepository.findAll(pageable)).thenReturn(promoCodePage);
        when(promoCodeMapper.toDto(any(PromoCode.class))).thenReturn(promoCodeDto);

        Page<PromoCodeDTO> result = promoCodeService.getAllPromoCodes(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("SUMMER2024");

        verify(promoCodeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("getPromoCodeById: Should return promo code when found")
    void getPromoCodeById_Success() {
        when(promoCodeRepository.findById(1L)).thenReturn(Optional.of(promoCodeEntity));
        when(promoCodeMapper.toDto(any(PromoCode.class))).thenReturn(promoCodeDto);

        PromoCodeDTO result = promoCodeService.getPromoCodeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("SUMMER2024");

        verify(promoCodeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getPromoCodeById: Should throw exception when not found")
    void getPromoCodeById_NotFound() {
        when(promoCodeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promoCodeService.getPromoCodeById(999L))
                .isInstanceOf(PromoCodeNotFoundException.class)
                .hasMessageContaining("not found");

        verify(promoCodeRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("updatePromoCode: Should update promo code successfully")
    void updatePromoCode_Success() {
        PromoCodeDTO updatedDto = new PromoCodeDTO(
                1L,
                "SUMMER2024",
                new BigDecimal("20.00"),
                "Summer sale 2024 - UPDATED",
                now,
                now.plusMonths(4),
                200,
                0,
                new BigDecimal("40.00"),
                true
        );

        when(promoCodeRepository.findById(1L)).thenReturn(Optional.of(promoCodeEntity));
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCodeEntity);
        when(promoCodeMapper.toDto(any(PromoCode.class))).thenReturn(updatedDto);

        PromoCodeDTO result = promoCodeService.updatePromoCode(1L, updatedDto);

        assertThat(result).isNotNull();
        verify(promoCodeRepository, times(1)).findById(1L);
        verify(promoCodeRepository, times(1)).save(any(PromoCode.class));
        verify(promoCodeMapper, times(1)).updateEntityFromDto(updatedDto, promoCodeEntity);
    }

    @Test
    @DisplayName("updatePromoCode: Should throw exception when code already exists")
    void updatePromoCode_DuplicateCode() {
        PromoCodeDTO updatedDto = new PromoCodeDTO(
                1L,
                "NEWCODE",
                new BigDecimal("20.00"),
                "Updated",
                now,
                now.plusMonths(4),
                200,
                0,
                new BigDecimal("40.00"),
                true
        );

        when(promoCodeRepository.findById(1L)).thenReturn(Optional.of(promoCodeEntity));
        when(promoCodeRepository.existsByCode("NEWCODE")).thenReturn(true);

        assertThatThrownBy(() -> promoCodeService.updatePromoCode(1L, updatedDto))
                .isInstanceOf(InvalidPromoCodeException.class)
                .hasMessageContaining("already exists");

        verify(promoCodeRepository, times(1)).findById(1L);
        verify(promoCodeRepository, never()).save(any(PromoCode.class));
    }

    @Test
    @DisplayName("deletePromoCode: Should soft delete promo code")
    void deletePromoCode_Success() {
        when(promoCodeRepository.findById(1L)).thenReturn(Optional.of(promoCodeEntity));
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCodeEntity);

        promoCodeService.deletePromoCode(1L);

        assertThat(promoCodeEntity.getIsActive()).isFalse();
        verify(promoCodeRepository, times(1)).findById(1L);
        verify(promoCodeRepository, times(1)).save(promoCodeEntity);
    }

    @Test
    @DisplayName("activatePromoCode: Should activate promo code")
    void activatePromoCode_Success() {
        promoCodeEntity.setIsActive(false);
        when(promoCodeRepository.findById(1L)).thenReturn(Optional.of(promoCodeEntity));
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCodeEntity);

        promoCodeService.activatePromoCode(1L);

        assertThat(promoCodeEntity.getIsActive()).isTrue();
        verify(promoCodeRepository, times(1)).findById(1L);
        verify(promoCodeRepository, times(1)).save(promoCodeEntity);
    }

    @Test
    @DisplayName("deactivatePromoCode: Should deactivate promo code")
    void deactivatePromoCode_Success() {
        when(promoCodeRepository.findById(1L)).thenReturn(Optional.of(promoCodeEntity));
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCodeEntity);

        promoCodeService.deactivatePromoCode(1L);

        assertThat(promoCodeEntity.getIsActive()).isFalse();
        verify(promoCodeRepository, times(1)).findById(1L);
        verify(promoCodeRepository, times(1)).save(promoCodeEntity);
    }

    @Test
    @DisplayName("validatePromoCode: Should return valid response for valid promo code")
    void validatePromoCode_Valid() {
        PromoCodeValidationRequestDTO request = new PromoCodeValidationRequestDTO(
                "SUMMER2024",
                new BigDecimal("100.00")
        );

        when(promoCodeRepository.findByCodeAndIsActiveTrue("SUMMER2024"))
                .thenReturn(Optional.of(promoCodeEntity));

        PromoCodeValidationResponseDTO result = promoCodeService.validatePromoCode(request);

        assertThat(result.valid()).isTrue();
        assertThat(result.message()).isEqualTo("Promo code is valid");
        assertThat(result.discountPercentage()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(result.finalAmount()).isEqualByComparingTo(new BigDecimal("85.00"));

        verify(promoCodeRepository, times(1)).findByCodeAndIsActiveTrue("SUMMER2024");
    }

    @Test
    @DisplayName("validatePromoCode: Should return invalid when promo code not found")
    void validatePromoCode_NotFound() {
        PromoCodeValidationRequestDTO request = new PromoCodeValidationRequestDTO(
                "INVALID",
                new BigDecimal("100.00")
        );

        when(promoCodeRepository.findByCodeAndIsActiveTrue("INVALID"))
                .thenReturn(Optional.empty());

        PromoCodeValidationResponseDTO result = promoCodeService.validatePromoCode(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).isEqualTo("Promo code not found or inactive");
        assertThat(result.finalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("validatePromoCode: Should return invalid when order amount is below minimum")
    void validatePromoCode_BelowMinimum() {
        PromoCodeValidationRequestDTO request = new PromoCodeValidationRequestDTO(
                "SUMMER2024",
                new BigDecimal("30.00")
        );

        when(promoCodeRepository.findByCodeAndIsActiveTrue("SUMMER2024"))
                .thenReturn(Optional.of(promoCodeEntity));

        PromoCodeValidationResponseDTO result = promoCodeService.validatePromoCode(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).contains("does not meet minimum requirement");
        assertThat(result.finalAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("validatePromoCode: Should return invalid when promo code has expired")
    void validatePromoCode_Expired() {
        PromoCodeValidationRequestDTO request = new PromoCodeValidationRequestDTO(
                "SUMMER2024",
                new BigDecimal("100.00")
        );

        LocalDateTime nowUtc = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        promoCodeEntity.setValidTo(nowUtc.minusDays(1));

        when(promoCodeRepository.findByCodeAndIsActiveTrue("SUMMER2024"))
                .thenReturn(Optional.of(promoCodeEntity));

        PromoCodeValidationResponseDTO result = promoCodeService.validatePromoCode(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).isEqualTo("Promo code has expired");
    }

    @Test
    @DisplayName("validatePromoCode: Should return invalid when promo code not yet valid")
    void validatePromoCode_NotYetValid() {
        PromoCodeValidationRequestDTO request = new PromoCodeValidationRequestDTO(
                "SUMMER2024",
                new BigDecimal("100.00")
        );

        LocalDateTime nowUtc = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        promoCodeEntity.setValidFrom(nowUtc.plusDays(1));

        when(promoCodeRepository.findByCodeAndIsActiveTrue("SUMMER2024"))
                .thenReturn(Optional.of(promoCodeEntity));

        PromoCodeValidationResponseDTO result = promoCodeService.validatePromoCode(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).isEqualTo("Promo code is not yet valid");
    }

    @Test
    @DisplayName("validatePromoCode: Should return invalid when usage limit reached")
    void validatePromoCode_UsageLimitReached() {
        PromoCodeValidationRequestDTO request = new PromoCodeValidationRequestDTO(
                "SUMMER2024",
                new BigDecimal("100.00")
        );

        LocalDateTime nowUtc = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        promoCodeEntity.setValidFrom(nowUtc.minusDays(1));
        promoCodeEntity.setValidTo(nowUtc.plusMonths(3));
        promoCodeEntity.setCurrentUses(100); // Equal to maxUses

        when(promoCodeRepository.findByCodeAndIsActiveTrue("SUMMER2024"))
                .thenReturn(Optional.of(promoCodeEntity));

        PromoCodeValidationResponseDTO result = promoCodeService.validatePromoCode(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).isEqualTo("Promo code usage limit reached");
    }

    @Test
    @DisplayName("incrementUsage: Should increment current uses")
    void incrementUsage_Success() {
        when(promoCodeRepository.findByCodeForUpdate("SUMMER2024")).thenReturn(Optional.of(promoCodeEntity));
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCodeEntity);

        promoCodeService.incrementUsage("SUMMER2024");

        assertThat(promoCodeEntity.getCurrentUses()).isEqualTo(1);
        verify(promoCodeRepository, times(1)).findByCodeForUpdate("SUMMER2024");
        verify(promoCodeRepository, times(1)).save(promoCodeEntity);
    }

    @Test
    @DisplayName("incrementUsage: Should throw exception when promo code not found")
    void incrementUsage_NotFound() {
        when(promoCodeRepository.findByCodeForUpdate("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promoCodeService.incrementUsage("INVALID"))
                .isInstanceOf(PromoCodeNotFoundException.class)
                .hasMessageContaining("not found");

        verify(promoCodeRepository, times(1)).findByCodeForUpdate("INVALID");
        verify(promoCodeRepository, never()).save(any(PromoCode.class));
    }
}
