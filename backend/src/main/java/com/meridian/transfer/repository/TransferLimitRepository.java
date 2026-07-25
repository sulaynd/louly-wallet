package com.meridian.transfer.repository;

import com.meridian.transfer.model.TransferLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferLimitRepository extends JpaRepository<TransferLimit, Long> {
    Optional<TransferLimit> findByCountryId(Long countryId);
}
