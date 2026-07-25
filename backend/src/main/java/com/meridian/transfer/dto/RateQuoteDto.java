package com.meridian.transfer.dto;

import java.math.BigDecimal;

public record RateQuoteDto(
        String fromCurrency,
        String toCurrency,
        BigDecimal rate,
        BigDecimal fee
) {
}
