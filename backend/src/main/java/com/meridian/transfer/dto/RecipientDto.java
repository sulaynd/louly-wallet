package com.meridian.transfer.dto;

import com.meridian.transfer.model.Recipient;
import com.meridian.transfer.model.RecipientType;

public record RecipientDto(
        Long id,
        String name,
        RecipientType type,
        String detail,
        String flagEmoji,
        String currencyCode,
        String phoneNumber,
        String receptionModeName,
        Long receptionModeId,
        String deliveryPartner,
        String address,
        String city
) {
    public static RecipientDto from(Recipient r) {
        return new RecipientDto(r.getId(), r.getName(), r.getType(), r.getDetail(), r.getFlagEmoji(),
                r.getCurrencyCode(), r.getPhoneNumber(), r.getReceptionModeName(),
                r.getReceivingReceptionMode() != null ? r.getReceivingReceptionMode().getId() : null,
                r.getDeliveryPartner(), r.getAddress(), r.getCity());
    }
}
