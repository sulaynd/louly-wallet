package com.meridian.transfer.controller;

import com.meridian.transfer.dto.TransferLimitsDto;
import com.meridian.transfer.dto.UpdateLimitRequest;
import com.meridian.transfer.model.TransferLimit;
import com.meridian.transfer.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/** Customer-service only (ROLE_ADMIN) — see SecurityConfig, which protects all of /api/admin/**. */
@RestController
@RequestMapping("/api/admin/limits")
public class AdminLimitController {

    private final TransferService transferService;

    public AdminLimitController(TransferService transferService) {
        this.transferService = transferService;
    }

    /** One country's current bounds: /api/admin/limits?countryId=4 (defaults to Canada if omitted). */
    @GetMapping
    public TransferLimitsDto current(@RequestParam(required = false) Long countryId) {
        return toDto(transferService.getLimits(countryId));
    }

    @PutMapping
    public TransferLimitsDto update(@Valid @RequestBody UpdateLimitRequest request, Principal principal) {
        transferService.setLimits(request.countryId(), request.minAmount(), request.maxAmount(),
                request.dailyMaxAmount(), request.monthlyMaxAmount(), principal.getName());
        return toDto(transferService.getLimits(request.countryId()));
    }

    private TransferLimitsDto toDto(TransferLimit limit) {
        return new TransferLimitsDto(limit.getMinAmount(), limit.getMaxAmount(),
                limit.getDailyMaxAmount(), limit.getMonthlyMaxAmount());
    }
}
