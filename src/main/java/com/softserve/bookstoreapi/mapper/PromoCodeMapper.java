package com.softserve.bookstoreapi.mapper;

import com.softserve.bookstoreapi.dto.PromoCodeDTO;
import com.softserve.bookstoreapi.model.PromoCode;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class PromoCodeMapper {

    public PromoCodeDTO toDto(PromoCode promoCode) {
        return new PromoCodeDTO(
                promoCode.getId(),
                promoCode.getCode(),
                promoCode.getDiscountPercentage(),
                promoCode.getDescription(),
                promoCode.getValidFrom(),
                promoCode.getValidTo(),
                promoCode.getMaxUses(),
                promoCode.getCurrentUses(),
                promoCode.getMinOrderAmount(),
                promoCode.getIsActive()
        );
    }

    public PromoCode toEntity(PromoCodeDTO dto) {
        PromoCode promoCode = new PromoCode();
        promoCode.setCode(dto.code());
        promoCode.setDiscountPercentage(dto.discountPercentage());
        promoCode.setDescription(dto.description());
        promoCode.setValidFrom(dto.validFrom());
        promoCode.setValidTo(dto.validTo());
        promoCode.setMaxUses(dto.maxUses());
        promoCode.setCurrentUses(dto.currentUses() != null ? dto.currentUses() : 0);
        promoCode.setMinOrderAmount(dto.minOrderAmount() != null ? dto.minOrderAmount() : BigDecimal.ZERO);
        return promoCode;
    }

    public void updateEntityFromDto(PromoCodeDTO dto, PromoCode promoCode) {
        promoCode.setCode(dto.code());
        promoCode.setDiscountPercentage(dto.discountPercentage());
        promoCode.setDescription(dto.description());
        promoCode.setValidFrom(dto.validFrom());
        promoCode.setValidTo(dto.validTo());
        promoCode.setMaxUses(dto.maxUses());
        if (dto.minOrderAmount() != null) {
            promoCode.setMinOrderAmount(dto.minOrderAmount());
        }
    }
}
