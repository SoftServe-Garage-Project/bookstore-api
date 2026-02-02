package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.softserve.bookstoreapi.controller.PromoCodeController;
import com.softserve.bookstoreapi.dto.PromoCodeDTO;
import com.softserve.bookstoreapi.dto.PromoCodeValidationRequestDTO;
import com.softserve.bookstoreapi.dto.PromoCodeValidationResponseDTO;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromoCodeController Tests")
class PromoCodeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PromoCodeService promoCodeService;

    @InjectMocks
    private PromoCodeController promoCodeController;

    private PromoCodeDTO validPromoCodeDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(promoCodeController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        now = LocalDateTime.of(2024, 6, 1, 0, 0);

        validPromoCodeDto = new PromoCodeDTO(
                1L,
                "SUMMER2024",
                new BigDecimal("15.00"),
                "Summer sale 2024",
                now,
                now.plusMonths(3),
                100,
                0,
                new BigDecimal("50.00"),
                true
        );
    }

    @Test
    @DisplayName("createPromoCode: Should return 201 and created DTO on success")
    void createPromoCode_Success() throws Exception {
        when(promoCodeService.createPromoCode(any(PromoCodeDTO.class))).thenReturn(validPromoCodeDto);

        mockMvc.perform(post("/api/promo-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPromoCodeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("SUMMER2024"))
                .andExpect(jsonPath("$.discountPercentage").value(15.00))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(promoCodeService, times(1)).createPromoCode(any(PromoCodeDTO.class));
    }

    @Test
    @DisplayName("createPromoCode: Should return 400 when validation fails")
    void createPromoCode_ValidationFail() throws Exception {
        PromoCodeDTO invalidDto = new PromoCodeDTO(
                null,
                "", // Invalid: empty code
                new BigDecimal("0.00"), // Invalid: too low
                null,
                now,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/promo-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(promoCodeService, never()).createPromoCode(any(PromoCodeDTO.class));
    }

    @Test
    @DisplayName("getAllPromoCodes: Should return 200 and Page of PromoCodes")
    void getAllPromoCodes_Success() throws Exception {
        List<PromoCodeDTO> promoCodeList = Collections.singletonList(validPromoCodeDto);
        Page<PromoCodeDTO> page = new PageImpl<>(promoCodeList, PageRequest.of(0, 10), 1);

        when(promoCodeService.getAllPromoCodes(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/promo-codes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].code").value("SUMMER2024"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(promoCodeService, times(1)).getAllPromoCodes(any(Pageable.class));
    }

    @Test
    @DisplayName("getPromoCodeById: Should return 200 and DTO on success")
    void getPromoCodeById_Success() throws Exception {
        when(promoCodeService.getPromoCodeById(1L)).thenReturn(validPromoCodeDto);

        mockMvc.perform(get("/api/promo-codes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("SUMMER2024"));

        verify(promoCodeService, times(1)).getPromoCodeById(1L);
    }

    @Test
    @DisplayName("updatePromoCode: Should return 200 and updated DTO on success")
    void updatePromoCode_Success() throws Exception {
        PromoCodeDTO updatedDto = new PromoCodeDTO(
                1L,
                "SUMMER2024",
                new BigDecimal("20.00"), // Updated discount
                "Summer sale 2024 - UPDATED",
                now,
                now.plusMonths(4),
                200,
                0,
                new BigDecimal("40.00"),
                true
        );

        when(promoCodeService.updatePromoCode(eq(1L), any(PromoCodeDTO.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/promo-codes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountPercentage").value(20.00))
                .andExpect(jsonPath("$.description").value("Summer sale 2024 - UPDATED"));

        verify(promoCodeService, times(1)).updatePromoCode(eq(1L), any(PromoCodeDTO.class));
    }

    @Test
    @DisplayName("deletePromoCode: Should return 204 on success")
    void deletePromoCode_Success() throws Exception {
        doNothing().when(promoCodeService).deletePromoCode(1L);

        mockMvc.perform(delete("/api/promo-codes/1"))
                .andExpect(status().isNoContent());

        verify(promoCodeService, times(1)).deletePromoCode(1L);
    }

    @Test
    @DisplayName("activatePromoCode: Should return 200 on success")
    void activatePromoCode_Success() throws Exception {
        doNothing().when(promoCodeService).activatePromoCode(1L);

        mockMvc.perform(patch("/api/promo-codes/1/activate"))
                .andExpect(status().isOk());

        verify(promoCodeService, times(1)).activatePromoCode(1L);
    }

    @Test
    @DisplayName("deactivatePromoCode: Should return 200 on success")
    void deactivatePromoCode_Success() throws Exception {
        doNothing().when(promoCodeService).deactivatePromoCode(1L);

        mockMvc.perform(patch("/api/promo-codes/1/deactivate"))
                .andExpect(status().isOk());

        verify(promoCodeService, times(1)).deactivatePromoCode(1L);
    }

    @Test
    @DisplayName("validatePromoCode: Should return 200 with valid response when promo code is valid")
    void validatePromoCode_Valid() throws Exception {
        PromoCodeValidationRequestDTO request = new PromoCodeValidationRequestDTO(
                "SUMMER2024",
                new BigDecimal("100.00")
        );

        PromoCodeValidationResponseDTO response = new PromoCodeValidationResponseDTO(
                true,
                "Promo code is valid",
                new BigDecimal("15.00"),
                new BigDecimal("15.00"),
                new BigDecimal("85.00")
        );

        when(promoCodeService.validatePromoCode(any(PromoCodeValidationRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/promo-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("Promo code is valid"))
                .andExpect(jsonPath("$.discountPercentage").value(15.00))
                .andExpect(jsonPath("$.discountAmount").value(15.00))
                .andExpect(jsonPath("$.finalAmount").value(85.00));

        verify(promoCodeService, times(1)).validatePromoCode(any(PromoCodeValidationRequestDTO.class));
    }

    @Test
    @DisplayName("validatePromoCode: Should return 200 with invalid response when promo code not found")
    void validatePromoCode_NotFound() throws Exception {
        PromoCodeValidationRequestDTO request = new PromoCodeValidationRequestDTO(
                "INVALID",
                new BigDecimal("100.00")
        );

        PromoCodeValidationResponseDTO response = new PromoCodeValidationResponseDTO(
                false,
                "Promo code not found or inactive",
                null,
                null,
                new BigDecimal("100.00")
        );

        when(promoCodeService.validatePromoCode(any(PromoCodeValidationRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/promo-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Promo code not found or inactive"))
                .andExpect(jsonPath("$.finalAmount").value(100.00));

        verify(promoCodeService, times(1)).validatePromoCode(any(PromoCodeValidationRequestDTO.class));
    }

    @Test
    @DisplayName("validatePromoCode: Should return 200 with invalid response when order amount is too low")
    void validatePromoCode_InsufficientOrderAmount() throws Exception {
        PromoCodeValidationRequestDTO request = new PromoCodeValidationRequestDTO(
                "SUMMER2024",
                new BigDecimal("30.00")
        );

        PromoCodeValidationResponseDTO response = new PromoCodeValidationResponseDTO(
                false,
                "Order amount does not meet minimum requirement of 50.00",
                null,
                null,
                new BigDecimal("30.00")
        );

        when(promoCodeService.validatePromoCode(any(PromoCodeValidationRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/promo-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Order amount does not meet minimum requirement of 50.00"));

        verify(promoCodeService, times(1)).validatePromoCode(any(PromoCodeValidationRequestDTO.class));
    }

    @Test
    @DisplayName("validatePromoCode: Should return 400 when validation fails")
    void validatePromoCode_ValidationFail() throws Exception {
        PromoCodeValidationRequestDTO invalidRequest = new PromoCodeValidationRequestDTO(
                "", // Invalid: empty code
                new BigDecimal("-10.00") // Invalid: negative amount
        );

        mockMvc.perform(post("/api/promo-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(promoCodeService, never()).validatePromoCode(any(PromoCodeValidationRequestDTO.class));
    }
}
