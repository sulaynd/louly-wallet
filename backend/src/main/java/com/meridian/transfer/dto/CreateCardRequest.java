package com.meridian.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Only the full card number is ever received here, and only to derive the network + last 4
 * digits — it is never persisted. Same for cvc: received (as a real payment-gateway integration
 * would need, to verify the card at the moment it's added) but discarded immediately after use —
 * never written to the database, never logged, never part of any entity field.
 * <p>
 * countryId determines the card's currency — pre-filled with the account holder's own country
 * on the frontend, but changeable, since a card isn't always issued in the same country the
 * person is registered in. A real integration would derive this from the card's BIN instead of
 * asking the person directly.
 */
public record CreateCardRequest(
        @NotBlank String cardHolderName,

        @NotBlank @Pattern(regexp = "\\d{13,19}", message = "Card number must be 13-19 digits")
        String cardNumber,

        @NotBlank @Pattern(regexp = "(0[1-9]|1[0-2])", message = "Expiry month must be 01-12")
        String expiryMonth,

        @NotBlank @Pattern(regexp = "\\d{4}", message = "Expiry year must be 4 digits, e.g. 2028")
        String expiryYear,

        @NotBlank @Pattern(regexp = "\\d{3,4}", message = "CVC must be 3-4 digits")
        String cvc,

        @NotNull Long countryId
) {
}
