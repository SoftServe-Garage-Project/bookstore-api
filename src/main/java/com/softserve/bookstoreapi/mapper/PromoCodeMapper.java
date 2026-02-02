package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.dto.PromoCodeDTO;
import com.softserve.bookstoreapi.model.PromoCode;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PromoCodeMapper {

    private final ModelMapper modelMapper;

    public PromoCodeDTO toDto(PromoCode promoCode) {
        return modelMapper.map(promoCode, PromoCodeDTO.class);
    }

    public PromoCode toEntity(PromoCodeDTO dto) {
        PromoCode promoCode = modelMapper.map(dto, PromoCode.class);

        // Set default values if null
        if (promoCode.getCurrentUses() == null) {
            promoCode.setCurrentUses(0);
        }
        if (promoCode.getMinOrderAmount() == null) {
            promoCode.setMinOrderAmount(BigDecimal.ZERO);
        }

        return promoCode;
    }

    public void updateEntityFromDto(PromoCodeDTO dto, PromoCode promoCode) {
        modelMapper.map(dto, promoCode);
    }
}
