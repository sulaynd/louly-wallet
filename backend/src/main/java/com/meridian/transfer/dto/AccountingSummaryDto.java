package com.meridian.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AccountingSummaryDto(
        Instant periodFrom,
        Instant periodTo,
        long transactionCount,
        BigDecimal totalPlatformRevenueCAD,
        List<CurrencyAmount> receptionModeCommissionExpenseByCurrency,
        List<CurrencyAmount> receptionModePrincipalOwedByCurrency
) {
    public record CurrencyAmount(String currency, BigDecimal amount) {
    }
}
