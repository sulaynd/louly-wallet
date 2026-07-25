package com.meridian.transfer.repository;

import com.meridian.transfer.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findByActiveTrue();
    Optional<Country> findByName(String name);
}
