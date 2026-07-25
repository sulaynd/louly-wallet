package com.meridian.transfer.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCountryStatusRequest(
        @NotNull Boolean active
) {
}
