package com.meridian.transfer.controller;

import com.meridian.transfer.dto.ReceptionModeDto;
import com.meridian.transfer.repository.ReceptionModeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public — needed for the "Partenaire de livraison" dropdown in the add-recipient form. */
@RestController
public class ReceptionModeController {

    private final ReceptionModeRepository receptionModeRepository;

    public ReceptionModeController(ReceptionModeRepository receptionModeRepository) {
        this.receptionModeRepository = receptionModeRepository;
    }

    @GetMapping("/api/reception-modes/deliverable")
    public List<ReceptionModeDto> deliverable() {
        return receptionModeRepository.findByLivrableTrue().stream().map(ReceptionModeDto::from).toList();
    }
}
