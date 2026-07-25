package com.meridian.transfer.service;

import java.math.BigDecimal;

/**
 * Mimics the shape of a real bank/payment-gateway authorization response — approved/declined,
 * a reference code for the authorization (or the decline reason), and the balance the bank
 * reported at the time of the call.
 */
public record BankAuthorizationResult(
        boolean approved,
        String referenceCode,
        BigDecimal reportedBalance,
        String declineReason
) {
    public static BankAuthorizationResult approve(String referenceCode, BigDecimal reportedBalance) {
        return new BankAuthorizationResult(true, referenceCode, reportedBalance, null);
    }

    public static BankAuthorizationResult decline(String reason, BigDecimal reportedBalance) {
        return new BankAuthorizationResult(false, null, reportedBalance, reason);
    }
}
