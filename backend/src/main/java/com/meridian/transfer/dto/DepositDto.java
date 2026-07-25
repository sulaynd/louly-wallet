package com.meridian.transfer.dto;

import com.meridian.transfer.model.AccountMovement;

import java.math.BigDecimal;
import java.time.Instant;

public record DepositDto(
        Long id,
        BigDecimal amount,
        BigDecimal newBalance,
        String processedByUsername,
        Instant createdAt,
        String note
) {
    public static DepositDto from(AccountMovement m) {
        return new DepositDto(m.getId(), m.getAmount(), m.getBalanceAfter(),
                m.getProcessedByUser() != null ? m.getProcessedByUser().getUsername() : null,
                m.getCreatedAt(), m.getNote());
    }
}
