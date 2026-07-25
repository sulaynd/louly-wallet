package com.meridian.transfer.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** minAmount/maxAmount/dailyMaxAmount/monthlyMaxAmount can each be omitted (null) — only the
 *  ones provided get updated. */
public record UpdateLimitRequest(
        @NotNull Long countryId,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal dailyMaxAmount,
        BigDecimal monthlyMaxAmount
) {
}
