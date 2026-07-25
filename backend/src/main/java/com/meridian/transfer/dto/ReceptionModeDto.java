package com.meridian.transfer.dto;

import com.meridian.transfer.model.ReceptionMode;

public record ReceptionModeDto(
        Long id,
        String name,
        String descriptionFr,
        String descriptionEn,
        boolean livrable,
        boolean active
) {
    public static ReceptionModeDto from(ReceptionMode p) {
        return new ReceptionModeDto(p.getId(), p.getName(), p.getDescriptionFr(), p.getDescriptionEn(),
                p.isLivrable(), p.isActive());
    }
}
