package com.meridian.transfer.controller;

import com.meridian.transfer.dto.CountryDto;
import com.meridian.transfer.dto.ReceptionModeDto;
import com.meridian.transfer.repository.CountryRepository;
import com.meridian.transfer.repository.ReceptionModeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public — needed before the person has an account (registration form). Only active countries. */
@RestController
public class CountryController {

    private final CountryRepository countryRepository;
    private final ReceptionModeRepository receptionModeRepository;

    public CountryController(CountryRepository countryRepository, ReceptionModeRepository receptionModeRepository) {
        this.countryRepository = countryRepository;
        this.receptionModeRepository = receptionModeRepository;
    }

    @GetMapping("/api/countries")
    public List<CountryDto> list() {
        return countryRepository.findByActiveTrue().stream().map(CountryDto::from).toList();
    }

    /** The active receptionModes available for this country — includes country-specific ones (e.g.
     *  Wave ou Orange for Senegal) plus active global receptionModes like Louly Express, who can pay
     *  recipients directly in many countries. */
    @GetMapping("/api/countries/{id}/reception-modes")
    public List<ReceptionModeDto> receptionModes(@PathVariable Long id) {
        return receptionModeRepository.findActiveForCountryIncludingGlobal(id).stream().map(ReceptionModeDto::from).toList();
    }
}
