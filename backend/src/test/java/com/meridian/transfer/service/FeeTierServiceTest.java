package com.meridian.transfer.service;

import com.meridian.transfer.model.FeeTier;
import com.meridian.transfer.repository.FeeTierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeTierServiceTest {

    private static final Long SENEGAL = 4L;

    @Mock
    private FeeTierRepository feeTierRepository;

    private FeeTierService feeTierService;

    @BeforeEach
    void setUp() {
        feeTierService = new FeeTierService(feeTierRepository);
    }

    private FeeTier tier(BigDecimal min, BigDecimal max, BigDecimal feePercent) {
        FeeTier t = new FeeTier();
        t.setMinAmount(min);
        t.setMaxAmount(max);
        t.setFeePercent(feePercent);
        return t;
    }

    @Test
    void tierFor_picksTheBracketContainingTheAmount() {
        List<FeeTier> senegalTiers = List.of(
                tier(new BigDecimal("500"), new BigDecimal("40000"), new BigDecimal("3")),
                tier(new BigDecimal("40001"), new BigDecimal("120000"), new BigDecimal("4")),
                tier(new BigDecimal("120001"), new BigDecimal("200000"), new BigDecimal("5"))
        );
        when(feeTierRepository.findByCountryIdOrderByMinAmountAsc(SENEGAL)).thenReturn(senegalTiers);

        Optional<FeeTier> tier = feeTierService.tierFor(new BigDecimal("20000"), SENEGAL);

        assertThat(tier).isPresent();
        assertThat(tier.get().getFeePercent()).isEqualByComparingTo("3");
    }

    @Test
    void tierFor_returnsEmpty_whenAmountIsBelowEveryTier() {
        when(feeTierRepository.findByCountryIdOrderByMinAmountAsc(SENEGAL))
                .thenReturn(List.of(tier(new BigDecimal("500"), new BigDecimal("40000"), new BigDecimal("3"))));

        Optional<FeeTier> tier = feeTierService.tierFor(new BigDecimal("100"), SENEGAL);

        assertThat(tier).isEmpty();
    }

    @Test
    void tierFor_returnsEmpty_whenAmountExceedsEveryTiersCeiling() {
        when(feeTierRepository.findByCountryIdOrderByMinAmountAsc(SENEGAL))
                .thenReturn(List.of(tier(new BigDecimal("500"), new BigDecimal("40000"), new BigDecimal("3"))));

        Optional<FeeTier> tier = feeTierService.tierFor(new BigDecimal("6000000"), SENEGAL);

        assertThat(tier).isEmpty();
    }

    @Test
    void tierFor_treatsAnOpenEndedTopTierAsHavingNoCeiling() {
        when(feeTierRepository.findByCountryIdOrderByMinAmountAsc(SENEGAL))
                .thenReturn(List.of(tier(new BigDecimal("500"), null, new BigDecimal("3"))));

        Optional<FeeTier> tier = feeTierService.tierFor(new BigDecimal("999999999"), SENEGAL);

        assertThat(tier).isPresent();
    }

    @Test
    void tierFor_returnsEmpty_whenAmountOrCountryIdIsNull() {
        assertThat(feeTierService.tierFor(null, SENEGAL)).isEmpty();
        assertThat(feeTierService.tierFor(new BigDecimal("100"), null)).isEmpty();
    }

    @Test
    void feeFor_appliesTheMatchingTiersPercentToTheAmount() {
        when(feeTierRepository.findByCountryIdOrderByMinAmountAsc(SENEGAL))
                .thenReturn(List.of(tier(new BigDecimal("500"), new BigDecimal("40000"), new BigDecimal("3"))));

        // The exact scenario confirmed during manual testing: 20 000 XOF @ 3% = 600 XOF.
        BigDecimal fee = feeTierService.feeFor(new BigDecimal("20000"), SENEGAL);

        assertThat(fee).isEqualByComparingTo("600.0000");
    }

    @Test
    void feeFor_isZero_whenNoTierMatches() {
        when(feeTierRepository.findByCountryIdOrderByMinAmountAsc(SENEGAL)).thenReturn(List.of());

        BigDecimal fee = feeTierService.feeFor(new BigDecimal("50"), SENEGAL);

        assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
