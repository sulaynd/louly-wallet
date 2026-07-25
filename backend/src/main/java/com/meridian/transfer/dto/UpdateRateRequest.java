package com.meridian.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateRateRequest(
        @NotNull @DecimalMin(value = "0.000001") BigDecimal rate
) {
}
