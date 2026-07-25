package com.meridian.transfer.dto;

import com.meridian.transfer.model.AccountType;
import com.meridian.transfer.model.UserAccount;

import java.math.BigDecimal;

public record UserAccountDto(
        Long id,
        AccountType type,
        String currencyCode,
        BigDecimal balance,
        /** Display string — "Compte dépôt Louly Express" for DEPOT, "Visa •••• 1234" for BANCAIRE. */
        String label,
        /** BANCAIRE only — null for DEPOT. */
        String cardNetwork,
        String cardLast4,
        String cardExpiryMonth,
        String cardExpiryYear
) {
    public static UserAccountDto from(UserAccount a) {
        String displayLabel = a.getType() == AccountType.DEPOT
                ? a.getLabel()
                : cardDisplayLabel(a.getCardNetwork(), a.getCardLast4());
        return new UserAccountDto(a.getId(), a.getType(), a.getCurrencyCode(), a.getBalance(),
                displayLabel, a.getCardNetwork(), a.getCardLast4(), a.getCardExpiryMonth(), a.getCardExpiryYear());
    }

    public static String cardDisplayLabel(String network, String last4) {
        String niceNetwork = switch (network == null ? "" : network) {
            case "VISA" -> "Visa";
            case "MASTERCARD" -> "Mastercard";
            case "AMEX" -> "Amex";
            default -> "Carte";
        };
        return last4 != null ? niceNetwork + " •••• " + last4 : niceNetwork;
    }
}
