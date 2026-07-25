package com.meridian.transfer.repository;

import com.meridian.transfer.model.CommissionRate;
import com.meridian.transfer.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommissionRateRepository extends JpaRepository<CommissionRate, Long> {
    Optional<CommissionRate> findByType(TransactionType type);
}
