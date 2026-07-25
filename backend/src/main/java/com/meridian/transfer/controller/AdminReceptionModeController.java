package com.meridian.transfer.controller;

import com.meridian.transfer.dto.ReceptionModeDto;
import com.meridian.transfer.dto.UpdateReceptionModeRequest;
import com.meridian.transfer.repository.ReceptionModeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Customer-service only (ROLE_ADMIN) — see SecurityConfig. */
@RestController
@RequestMapping("/api/admin/reception-modes")
public class AdminReceptionModeController {

    private final ReceptionModeRepository receptionModeRepository;

    public AdminReceptionModeController(ReceptionModeRepository receptionModeRepository) {
        this.receptionModeRepository = receptionModeRepository;
    }

    /** Every receptionMode across every country, plus the country-less delivery ones. */
    @GetMapping
    public List<ReceptionModeDto> list() {
        return receptionModeRepository.findAll().stream().map(ReceptionModeDto::from).toList();
    }

    /** Updates whichever of active/livrable is provided (either can be omitted). */
    @PutMapping("/{id}")
    public ReceptionModeDto update(@PathVariable Long id, @RequestBody UpdateReceptionModeRequest request) {
        var receptionMode = receptionModeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReceptionMode not found: " + id));
        if (request.active() != null) {
            receptionMode.setActive(request.active());
        }
        if (request.livrable() != null) {
            receptionMode.setLivrable(request.livrable());
        }
        return ReceptionModeDto.from(receptionModeRepository.save(receptionMode));
    }
}
