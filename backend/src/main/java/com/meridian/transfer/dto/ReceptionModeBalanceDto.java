package com.meridian.transfer.dto;

import java.math.BigDecimal;

public record ReceptionModeBalanceDto(
        Long receptionModeId,
        String receptionModeName,
        String currency,
        BigDecimal totalOwed,
        BigDecimal totalSettled,
        BigDecimal currentBalance
) {
}
