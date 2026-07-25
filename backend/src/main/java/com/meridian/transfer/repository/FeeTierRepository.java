package com.meridian.transfer.repository;

import com.meridian.transfer.model.FeeTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeTierRepository extends JpaRepository<FeeTier, Long> {
    List<FeeTier> findAllByOrderByMinAmountAsc();
    List<FeeTier> findByCountryIdOrderByMinAmountAsc(Long countryId);
}
