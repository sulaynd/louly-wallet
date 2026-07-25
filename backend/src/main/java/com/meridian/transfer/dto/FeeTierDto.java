package com.meridian.transfer.dto;

import com.meridian.transfer.model.FeeTier;

import java.math.BigDecimal;

public record FeeTierDto(
        Long id,
        Long countryId,
        String countryName,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal feePercent
) {
    public static FeeTierDto from(FeeTier t) {
        return new FeeTierDto(
                t.getId(),
                t.getCountry() != null ? t.getCountry().getId() : null,
                t.getCountry() != null ? t.getCountry().getName() : null,
                t.getMinAmount(), t.getMaxAmount(), t.getFeePercent());
    }
}
