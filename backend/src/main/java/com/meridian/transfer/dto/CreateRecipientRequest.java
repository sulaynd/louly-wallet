package com.meridian.transfer.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRecipientRequest(
        @NotBlank String name,
        /** Optional — e.g. bank/account info. Not every recipient has one (mobile money top-up
         *  recipients, for instance, once that's implemented). */
        String detail,
        /** National vs international, currency, and flag are all inferred from this number's
         *  calling code — see PhoneCountryResolver. */
        @NotBlank String phoneNumber,
        /** e.g. "Wave ou Orange", "BNB Cash Pickup", "Compte bancaire" */
        @NotBlank String receptionModeName,
        /** e.g. "Louly Express", "Orange Money", "Wave" */
        @NotBlank String deliveryPartner,
        @NotBlank String address,
        @NotBlank String city
) {
}
