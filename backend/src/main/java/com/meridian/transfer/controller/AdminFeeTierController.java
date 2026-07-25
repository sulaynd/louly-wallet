package com.meridian.transfer.controller;

import com.meridian.transfer.dto.CreateFeeTierRequest;
import com.meridian.transfer.dto.FeeTierDto;
import com.meridian.transfer.dto.UpdateFeeTierRequest;
import com.meridian.transfer.model.Country;
import com.meridian.transfer.model.FeeTier;
import com.meridian.transfer.repository.CountryRepository;
import com.meridian.transfer.repository.FeeTierRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/** Customer-service only (ROLE_ADMIN) — see SecurityConfig. */
@RestController
@RequestMapping("/api/admin/fee-tiers")
public class AdminFeeTierController {

    private final FeeTierRepository feeTierRepository;
    private final CountryRepository countryRepository;

    public AdminFeeTierController(FeeTierRepository feeTierRepository, CountryRepository countryRepository) {
        this.feeTierRepository = feeTierRepository;
        this.countryRepository = countryRepository;
    }

    /** Optionally filter to one country's grid: /api/admin/fee-tiers?countryId=4 */
    @GetMapping
    public List<FeeTierDto> list(@RequestParam Optional<Long> countryId) {
        List<FeeTier> tiers = countryId
                .map(feeTierRepository::findByCountryIdOrderByMinAmountAsc)
                .orElseGet(feeTierRepository::findAllByOrderByMinAmountAsc);
        return tiers.stream().map(FeeTierDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<FeeTierDto> create(@Valid @RequestBody CreateFeeTierRequest request) {
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new EntityNotFoundException("Country not found: " + request.countryId()));

        FeeTier tier = new FeeTier();
        tier.setCountry(country);
        tier.setMinAmount(request.minAmount());
        tier.setMaxAmount(request.maxAmount());
        tier.setFeePercent(request.feePercent());
        return ResponseEntity.status(HttpStatus.CREATED).body(FeeTierDto.from(feeTierRepository.save(tier)));
    }

    @PutMapping("/{id}")
    public FeeTierDto update(@PathVariable Long id, @RequestBody UpdateFeeTierRequest request) {
        FeeTier tier = feeTierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fee tier not found: " + id));
        if (request.countryId() != null) {
            Country country = countryRepository.findById(request.countryId())
                    .orElseThrow(() -> new EntityNotFoundException("Country not found: " + request.countryId()));
            tier.setCountry(country);
        }
        if (request.minAmount() != null) {
            tier.setMinAmount(request.minAmount());
        }
        if (request.maxAmount() != null) {
            tier.setMaxAmount(request.maxAmount());
        }
        if (request.feePercent() != null) {
            tier.setFeePercent(request.feePercent());
        }
        return FeeTierDto.from(feeTierRepository.save(tier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feeTierRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
