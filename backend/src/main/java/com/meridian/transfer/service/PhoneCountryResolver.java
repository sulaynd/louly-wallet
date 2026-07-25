package com.meridian.transfer.service;

import com.meridian.transfer.model.Country;
import com.meridian.transfer.repository.CountryRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;

/**
 * Infers a recipient's country (and therefore flag, currency, and national/international type)
 * from the calling code prefix of their phone number — no need to ask for it separately.
 * Only considers ACTIVE countries from the {@code countries} table, so deactivating a country in
 * the backoffice also stops new recipients from being auto-detected as that country.
 * <p>
 * Note: +1 covers both Canada and the US/Caribbean in real life (NANP); this simplified demo
 * always resolves +1 to whichever active country has that calling code first (Canada, by seed order).
 */
@Service
public class PhoneCountryResolver {

    public record Resolution(String country, String flagEmoji, String currencyCode, boolean isCanadian) {
    }

    private final CountryRepository countryRepository;

    public PhoneCountryResolver(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public Optional<Resolution> resolve(String phoneNumber) {
        if (phoneNumber == null) {
            return Optional.empty();
        }
        String cleaned = phoneNumber.trim().replace(" ", "");

        return countryRepository.findByActiveTrue().stream()
                .filter(c -> c.getCallingCode() != null && cleaned.startsWith(c.getCallingCode()))
                // Longest calling code wins, so a 3-digit code isn't shadowed by a shorter one.
                .max(Comparator.comparingInt(c -> c.getCallingCode().length()))
                .map(this::toResolution);
    }

    private Resolution toResolution(Country country) {
        return new Resolution(
                country.getName(),
                country.getFlagEmoji(),
                country.getCurrencyCode(),
                "Canada".equals(country.getName())
        );
    }
}
