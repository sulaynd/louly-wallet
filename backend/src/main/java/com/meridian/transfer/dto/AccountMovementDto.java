package com.meridian.transfer.dto;

import com.meridian.transfer.model.AccountMovement;
import com.meridian.transfer.model.MovementType;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountMovementDto(
        Long id,
        MovementType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Long relatedTransferId,
        String processedByUsername,
        Instant createdAt,
        String note
) {
    public static AccountMovementDto from(AccountMovement m) {
        return new AccountMovementDto(
                m.getId(),
                m.getType(),
                m.getAmount(),
                m.getBalanceAfter(),
                m.getRelatedTransfer() != null ? m.getRelatedTransfer().getId() : null,
                m.getProcessedByUser() != null ? m.getProcessedByUser().getUsername() : null,
                m.getCreatedAt(),
                m.getNote()
        );
    }
}
