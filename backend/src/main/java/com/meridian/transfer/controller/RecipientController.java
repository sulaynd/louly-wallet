package com.meridian.transfer.controller;

import com.meridian.transfer.dto.CreateRecipientRequest;
import com.meridian.transfer.dto.RecipientDto;
import com.meridian.transfer.model.AppUser;
import com.meridian.transfer.model.Country;
import com.meridian.transfer.model.ReceptionMode;
import com.meridian.transfer.model.Recipient;
import com.meridian.transfer.model.RecipientType;
import com.meridian.transfer.repository.AppUserRepository;
import com.meridian.transfer.repository.CountryRepository;
import com.meridian.transfer.repository.ReceptionModeRepository;
import com.meridian.transfer.repository.RecipientRepository;
import com.meridian.transfer.service.PhoneCountryResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class RecipientController {

    private final RecipientRepository recipientRepository;
    private final PhoneCountryResolver phoneCountryResolver;
    private final ReceptionModeRepository receptionModeRepository;
    private final CountryRepository countryRepository;
    private final AppUserRepository appUserRepository;

    public RecipientController(RecipientRepository recipientRepository,
                                PhoneCountryResolver phoneCountryResolver,
                                ReceptionModeRepository receptionModeRepository,
                                CountryRepository countryRepository,
                                AppUserRepository appUserRepository) {
        this.recipientRepository = recipientRepository;
        this.phoneCountryResolver = phoneCountryResolver;
        this.receptionModeRepository = receptionModeRepository;
        this.countryRepository = countryRepository;
        this.appUserRepository = appUserRepository;
    }

    /** Only the authenticated user's own recipients — this is now a private directory per account. */
    @GetMapping("/api/recipients")
    public List<RecipientDto> list(@RequestParam Optional<RecipientType> type, Principal principal) {
        List<Recipient> recipients = type
                .map(t -> recipientRepository.findByOwnerUsernameAndType(principal.getName(), t))
                .orElseGet(() -> recipientRepository.findByOwnerUsername(principal.getName()));
        return recipients.stream().map(RecipientDto::from).toList();
    }

    @PostMapping("/api/recipients")
    public ResponseEntity<?> create(@Valid @RequestBody CreateRecipientRequest request, Principal principal) {
        Optional<PhoneCountryResolver.Resolution> resolution = phoneCountryResolver.resolve(request.phoneNumber());

        if (resolution.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unrecognized or inactive country calling code in phone number."));
        }

        PhoneCountryResolver.Resolution info = resolution.get();

        Recipient recipient = new Recipient();
        recipient.setName(request.name());
        recipient.setDetail(request.detail() != null && !request.detail().isBlank() ? request.detail().trim() : null);
        recipient.setPhoneNumber(request.phoneNumber());
        recipient.setReceptionModeName(request.receptionModeName());
        recipient.setReceivingReceptionMode(resolveReceptionMode(request.receptionModeName(), info.currencyCode()));
        recipient.setDeliveryPartner(request.deliveryPartner());
        recipient.setAddress(request.address());
        recipient.setCity(request.city());
        recipient.setType(recipientTypeFor(info.currencyCode(), principal.getName()));
        recipient.setFlagEmoji(info.flagEmoji());
        recipient.setCurrencyCode(info.currencyCode());
        recipient.setOwnerUsername(principal.getName());

        Recipient saved = recipientRepository.save(recipient);
        return ResponseEntity.status(HttpStatus.CREATED).body(RecipientDto.from(saved));
    }

    /**
     * NATIONAL if the recipient's currency matches the sender's own account currency, regardless
     * of either party's country — a Senegalese sender adding a Senegalese recipient is NATIONAL,
     * not INTERNATIONAL just because neither is Canadian. Matches the same reasoning already used
     * in TransferService for the sent/received currency comparison. Falls back to INTERNATIONAL
     * if the sender's own currency can't be resolved (shouldn't normally happen).
     */
    private RecipientType recipientTypeFor(String recipientCurrencyCode, String ownerUsername) {
        String senderCurrency = appUserRepository.findByUsername(ownerUsername)
                .map(AppUser::getCountry)
                .flatMap(countryRepository::findByName)
                .map(Country::getCurrencyCode)
                .orElse(null);
        return senderCurrency != null && senderCurrency.equalsIgnoreCase(recipientCurrencyCode)
                ? RecipientType.NATIONAL
                : RecipientType.INTERNATIONAL;
    }

    /**
     * Finds the exact ReceptionMode row for this name in this country — resolves the ambiguity that
     * receptionModeName alone can't (e.g. "Compte bancaire" exists once per country). Falls back to
     * any match by name if no country-specific row is found (e.g. a country-less receptionMode).
     */
    private ReceptionMode resolveReceptionMode(String receptionModeName, String currencyCode) {
        if (receptionModeName == null || receptionModeName.isBlank()) {
            return null;
        }
        Optional<Country> country = countryRepository.findAll().stream()
                .filter(c -> currencyCode.equalsIgnoreCase(c.getCurrencyCode()))
                .findFirst();

        if (country.isPresent()) {
            Optional<ReceptionMode> countryMatch = receptionModeRepository.findByCountryId(country.get().getId()).stream()
                    .filter(p -> receptionModeName.equals(p.getName()))
                    .findFirst();
            if (countryMatch.isPresent()) {
                return countryMatch.get();
            }
        }
        return receptionModeRepository.findFirstByName(receptionModeName).orElse(null);
    }
}
