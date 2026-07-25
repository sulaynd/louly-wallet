package com.meridian.transfer.dto;

import com.meridian.transfer.model.LedgerEntryType;
import com.meridian.transfer.model.ReceptionModeLedgerEntry;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntryDto(
        Long id,
        String receptionModeName,
        Long transferId,
        LedgerEntryType type,
        BigDecimal amount,
        String currency,
        Instant createdAt,
        String note,
        String recordedBy
) {
    public static LedgerEntryDto from(ReceptionModeLedgerEntry e) {
        return new LedgerEntryDto(
                e.getId(),
                e.getReceptionMode() != null ? e.getReceptionMode().getName() : null,
                e.getTransfer() != null ? e.getTransfer().getId() : null,
                e.getType(),
                e.getAmount(),
                e.getCurrency(),
                e.getCreatedAt(),
                e.getNote(),
                e.getRecordedBy()
        );
    }
}
