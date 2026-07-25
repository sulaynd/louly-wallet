package com.meridian.transfer.dto;

/** Either field can be omitted (null) — only the ones provided get updated. */
public record UpdateReceptionModeRequest(
        Boolean active,
        Boolean livrable
) {
}
