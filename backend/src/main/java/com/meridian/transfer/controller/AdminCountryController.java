package com.meridian.transfer.controller;

import com.meridian.transfer.dto.CountryDto;
import com.meridian.transfer.dto.UpdateCountryStatusRequest;
import com.meridian.transfer.repository.CountryRepository;
import com.meridian.transfer.repository.FeeTierRepository;
import com.meridian.transfer.repository.TransferLimitRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Customer-service only (ROLE_ADMIN) — see SecurityConfig. */
@RestController
@RequestMapping("/api/admin/countries")
public class AdminCountryController {

    private final CountryRepository countryRepository;
    private final FeeTierRepository feeTierRepository;
    private final TransferLimitRepository transferLimitRepository;

    public AdminCountryController(CountryRepository countryRepository, FeeTierRepository feeTierRepository,
                                   TransferLimitRepository transferLimitRepository) {
        this.countryRepository = countryRepository;
        this.feeTierRepository = feeTierRepository;
        this.transferLimitRepository = transferLimitRepository;
    }

    /** All countries, active or not — the demo backoffice screen would show this list with toggles. */
    @GetMapping
    public List<CountryDto> list() {
        return countryRepository.findAll().stream().map(CountryDto::from).toList();
    }

    /**
     * Activating a country means real people can register and send money from it — so it can't
     * be flipped on until its fee grid and transfer bounds actually exist, in its own currency.
     * Without this check, a country with no fee_tiers silently charges a $0 fee (see
     * FeeTierService — no matching tier means no fee at all), and one with no transfer_limits
     * falls back to defaults sized for CAD, which may be wildly wrong for another currency.
     * Deactivating is always allowed — only turning a country ON requires this readiness check.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateCountryStatusRequest request) {
        var country = countryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Country not found: " + id));

        if (request.active()) {
            List<String> missing = new ArrayList<>();
            if (feeTierRepository.findByCountryIdOrderByMinAmountAsc(id).isEmpty()) {
                missing.add("fee_tiers");
            }
            if (transferLimitRepository.findByCountryId(id).isEmpty()) {
                missing.add("transfer_limits");
            }
            if (!missing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "error", "Cannot activate " + country.getName() + " — missing configuration: "
                                + String.join(", ", missing) + ". Set these up first via /api/admin/fee-tiers "
                                + "and /api/admin/limits, denominated in " + country.getCurrencyCode() + "."));
            }
        }

        country.setActive(request.active());
        return ResponseEntity.ok(CountryDto.from(countryRepository.save(country)));
    }
}
