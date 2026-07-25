package com.meridian.transfer.repository;

import com.meridian.transfer.model.ReceptionMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReceptionModeRepository extends JpaRepository<ReceptionMode, Long> {
    List<ReceptionMode> findByCountryId(Long countryId);
    List<ReceptionMode> findByCountryIdAndActiveTrue(Long countryId);
    List<ReceptionMode> findByLivrableTrue();
    Optional<ReceptionMode> findFirstByName(String name);

    /**
     * Active receptionModes for this country, PLUS active country-less/global receptionModes (e.g. Louly
     * Express, which can pay recipients directly in many countries) — so a global receptionMode shows
     * up as a reception-mode choice everywhere, not just where it has its own country row.
     */
    @Query("SELECT p FROM ReceptionMode p WHERE (p.country.id = :countryId OR p.country IS NULL) AND p.active = true")
    List<ReceptionMode> findActiveForCountryIncludingGlobal(@Param("countryId") Long countryId);
}
