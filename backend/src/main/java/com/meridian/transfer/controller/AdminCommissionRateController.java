package com.meridian.transfer.controller;

import com.meridian.transfer.dto.CommissionRateDto;
import com.meridian.transfer.dto.UpdateCommissionRateRequest;
import com.meridian.transfer.model.CommissionRate;
import com.meridian.transfer.repository.CommissionRateRepository;
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
@RequestMapping("/api/admin/commission-rates")
public class AdminCommissionRateController {

    private final CommissionRateRepository commissionRateRepository;

    public AdminCommissionRateController(CommissionRateRepository commissionRateRepository) {
        this.commissionRateRepository = commissionRateRepository;
    }

    @GetMapping
    public List<CommissionRateDto> list() {
        return commissionRateRepository.findAll().stream().map(CommissionRateDto::from).toList();
    }

    @PutMapping("/{id}")
    public CommissionRateDto update(@PathVariable Long id, @RequestBody UpdateCommissionRateRequest request) {
        CommissionRate rate = commissionRateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commission rate not found: " + id));
        if (request.partnerSharePercent() != null) {
            rate.setPartnerSharePercent(request.partnerSharePercent());
        }
        return CommissionRateDto.from(commissionRateRepository.save(rate));
    }
}
