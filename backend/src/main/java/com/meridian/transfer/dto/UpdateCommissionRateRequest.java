package com.meridian.transfer.dto;

import java.math.BigDecimal;

public record UpdateCommissionRateRequest(
        BigDecimal partnerSharePercent
) {
}
