package com.meridian.transfer.dto;

import com.meridian.transfer.model.Country;

public record CountryDto(
        Long id,
        String name,
        String flagEmoji,
        String currencyCode,
        String callingCode,
        boolean active
) {
    public static CountryDto from(Country c) {
        return new CountryDto(c.getId(), c.getName(), c.getFlagEmoji(), c.getCurrencyCode(), c.getCallingCode(), c.isActive());
    }
}
