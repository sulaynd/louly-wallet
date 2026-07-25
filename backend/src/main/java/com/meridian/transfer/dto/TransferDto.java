package com.meridian.transfer.dto;

import com.meridian.transfer.model.AccountType;
import com.meridian.transfer.model.RecipientType;
import com.meridian.transfer.model.TransactionType;
import com.meridian.transfer.model.Transfer;
import com.meridian.transfer.model.TransferEventType;
import com.meridian.transfer.model.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TransferDto(
        Long id,
        RecipientDto recipient,
        RecipientType mode,
        BigDecimal amountSent,
        BigDecimal amountReceived,
        String sourceCurrency,
        String targetCurrency,
        BigDecimal exchangeRate,
        BigDecimal fee,
        BigDecimal totalCharged,
        TransferStatus status,
        Instant createdAt,
        TransactionType transactionType,
        BigDecimal commissionRatePercent,
        BigDecimal platformCommissionAmount,
        String platformCommissionCurrency,
        BigDecimal receivingReceptionModeCommissionAmount,
        String receivingReceptionModeCommissionCurrency,
        AccountType sourceAccountType,
        String sourceAccountLabel,
        String bankAuthorizationReference,
        List<TransferEventDto> events
) {
    public record TransferEventDto(TransferEventType type, String title, String subtitle, boolean pending) {
    }

    public static TransferDto from(Transfer t) {
        List<TransferEventDto> events = t.getEvents().stream()
                .map(e -> new TransferEventDto(e.getType(), e.getTitle(), e.getSubtitle(), e.isPending()))
                .toList();
        return new TransferDto(
                t.getId(),
                RecipientDto.from(t.getRecipient()),
                t.getMode(),
                t.getAmountSent(),
                t.getAmountReceived(),
                t.getSourceCurrency(),
                t.getTargetCurrency(),
                t.getExchangeRate(),
                t.getFee(),
                t.getTotalCharged(),
                t.getStatus(),
                t.getCreatedAt(),
                t.getTransactionType(),
                t.getCommissionRatePercent(),
                t.getPlatformCommissionAmount(),
                t.getPlatformCommissionCurrency(),
                t.getReceivingReceptionModeCommissionAmount(),
                t.getReceivingReceptionModeCommissionCurrency(),
                t.getSourceAccountType(),
                t.getSourceAccountLabel(),
                t.getBankAuthorizationReference(),
                events
        );
    }
}
