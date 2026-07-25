package com.meridian.transfer.controller;

import com.meridian.transfer.dto.RateQuoteDto;
import com.meridian.transfer.model.AppUser;
import com.meridian.transfer.model.Country;
import com.meridian.transfer.repository.AppUserRepository;
import com.meridian.transfer.repository.CountryRepository;
import com.meridian.transfer.service.ExchangeRateService;
import com.meridian.transfer.service.FeeTierService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
public class RateController {

    private final ExchangeRateService exchangeRateService;
    private final FeeTierService feeTierService;
    private final AppUserRepository appUserRepository;
    private final CountryRepository countryRepository;

    public RateController(ExchangeRateService exchangeRateService, FeeTierService feeTierService,
                           AppUserRepository appUserRepository, CountryRepository countryRepository) {
        this.exchangeRateService = exchangeRateService;
        this.feeTierService = feeTierService;
        this.appUserRepository = appUserRepository;
        this.countryRepository = countryRepository;
    }

    @GetMapping("/api/rates")
    public RateQuoteDto quote(@RequestParam String from, @RequestParam String to,
                               @RequestParam(defaultValue = "0") BigDecimal amount,
                               Principal principal) {
        Long senderCountryId = resolveCountryId(principal);
        return new RateQuoteDto(
                from.toUpperCase(),
                to.toUpperCase(),
                exchangeRateService.rateFor(from, to),
                feeTierService.feeFor(amount, senderCountryId)
        );
    }

    /** Whose fee-tier grid applies — resolved from the authenticated account, not the currency
     *  code (which wouldn't disambiguate if two countries ever shared a currency). */
    private Long resolveCountryId(Principal principal) {
        if (principal == null) {
            return null;
        }
        return appUserRepository.findByUsername(principal.getName())
                .map(AppUser::getCountry)
                .flatMap(countryRepository::findByName)
                .map(Country::getId)
                .orElse(null);
    }
}
