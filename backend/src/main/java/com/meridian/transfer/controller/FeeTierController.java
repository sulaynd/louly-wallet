package com.meridian.transfer.controller;

import com.meridian.transfer.dto.FeeTierDto;
import com.meridian.transfer.repository.FeeTierRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/** Public — needed before the person necessarily has a token, and to show a country's own grid. */
@RestController
public class FeeTierController {

    private final FeeTierRepository feeTierRepository;

    public FeeTierController(FeeTierRepository feeTierRepository) {
        this.feeTierRepository = feeTierRepository;
    }

    /** Optionally filter to one country's grid: /api/fee-tiers?countryId=4 */
    @GetMapping("/api/fee-tiers")
    public List<FeeTierDto> list(@RequestParam Optional<Long> countryId) {
        return countryId
                .map(feeTierRepository::findByCountryIdOrderByMinAmountAsc)
                .orElseGet(feeTierRepository::findAllByOrderByMinAmountAsc)
                .stream().map(FeeTierDto::from).toList();
    }
}
