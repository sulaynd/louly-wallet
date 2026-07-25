package com.meridian.transfer.service;

import com.meridian.transfer.model.FeeTier;
import com.meridian.transfer.repository.FeeTierRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Tiers are per-country, denominated in that country's own currency — no exchange-rate
 * conversion needed to find the right bracket, and tiers stay meaningful even if rates move.
 */
@Service
public class FeeTierService {

    private final FeeTierRepository feeTierRepository;

    public FeeTierService(FeeTierRepository feeTierRepository) {
        this.feeTierRepository = feeTierRepository;
    }

    public Optional<FeeTier> tierFor(BigDecimal amount, Long countryId) {
        if (amount == null || countryId == null) {
            return Optional.empty();
        }
        return feeTierRepository.findByCountryIdOrderByMinAmountAsc(countryId).stream()
                .filter(t -> amount.compareTo(t.getMinAmount()) >= 0
                        && (t.getMaxAmount() == null || amount.compareTo(t.getMaxAmount()) <= 0))
                .findFirst();
    }

    /** The fee for this amount in this country's grid — zero if no tier matches. */
    public BigDecimal feeFor(BigDecimal amount, Long countryId) {
        return tierFor(amount, countryId)
                .map(t -> amount.multiply(t.getFeePercent()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
    }
}
