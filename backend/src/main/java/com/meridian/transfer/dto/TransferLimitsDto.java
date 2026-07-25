package com.meridian.transfer.dto;

import java.math.BigDecimal;

public record TransferLimitsDto(
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal dailyMaxAmount,
        BigDecimal monthlyMaxAmount
) {
}
