package com.meridian.transfer.dto;

import com.meridian.transfer.model.CommissionRate;
import com.meridian.transfer.model.TransactionType;

import java.math.BigDecimal;

public record CommissionRateDto(
        Long id,
        TransactionType type,
        String label,
        BigDecimal partnerSharePercent
) {
    public static CommissionRateDto from(CommissionRate r) {
        return new CommissionRateDto(r.getId(), r.getType(), r.getLabel(), r.getPartnerSharePercent());
    }
}
