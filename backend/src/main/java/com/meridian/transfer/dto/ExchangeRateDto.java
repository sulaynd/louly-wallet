package com.meridian.transfer.dto;

import com.meridian.transfer.model.ExchangeRate;
import com.meridian.transfer.model.RateSource;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateDto(
        String baseCurrency,
        String targetCurrency,
        BigDecimal rate,
        RateSource source,
        Instant updatedAt
) {
    public static ExchangeRateDto from(ExchangeRate rate) {
        return new ExchangeRateDto(
                rate.getBaseCurrency(),
                rate.getTargetCurrency(),
                rate.getRate(),
                rate.getSource(),
                rate.getUpdatedAt()
        );
    }
}
