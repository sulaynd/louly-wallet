package com.meridian.transfer.dto;

import java.math.BigDecimal;

/** Any field can be omitted (null) — only the ones provided get updated. */
public record UpdateFeeTierRequest(
        Long countryId,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal feePercent
) {
}
