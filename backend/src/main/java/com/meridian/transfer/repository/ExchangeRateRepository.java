package com.meridian.transfer.repository;

import com.meridian.transfer.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findByBaseCurrencyAndTargetCurrency(String baseCurrency, String targetCurrency);
    List<ExchangeRate> findByBaseCurrency(String baseCurrency);
}
