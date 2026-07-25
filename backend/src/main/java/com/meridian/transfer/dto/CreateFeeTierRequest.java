package com.meridian.transfer.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateFeeTierRequest(
        @NotNull Long countryId,
        @NotNull BigDecimal minAmount,
        /** Null = open-ended (no upper bound). */
        BigDecimal maxAmount,
        @NotNull BigDecimal feePercent
) {
}
