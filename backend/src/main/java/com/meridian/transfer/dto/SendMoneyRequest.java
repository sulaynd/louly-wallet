package com.meridian.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SendMoneyRequest(
        @NotNull Long recipientId,
        /** Which of the sender's own accounts is funding this transfer — compte dépôt or a bank
         *  account reference. Required so the right one gets debited (or not, for a bank ref). */
        @NotNull Long sourceAccountId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        /**
         * The exact "they receive", exchange rate, and fee the person already saw and confirmed
         * on screen. All three are optional (older clients can omit them and the backend falls
         * back to computing fresh values) but when present they're used as-is, frozen at the
         * moment the person clicked "Review transfer" — never re-derived from a rate that may
         * have changed in the meantime (e.g. the scheduled rate refresh running mid-transaction).
         * This guarantees the confirmation screen always matches exactly what was on screen when
         * the person confirmed, regardless of any rate update that happens a second later.
         */
        BigDecimal amountReceived,
        BigDecimal rate,
        BigDecimal fee
) {
}
