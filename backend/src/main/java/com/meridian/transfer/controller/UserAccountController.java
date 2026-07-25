package com.meridian.transfer.controller;

import com.meridian.transfer.dto.AccountMovementDto;
import com.meridian.transfer.dto.CreateCardRequest;
import com.meridian.transfer.dto.UserAccountDto;
import com.meridian.transfer.model.AccountType;
import com.meridian.transfer.model.AppUser;
import com.meridian.transfer.model.Country;
import com.meridian.transfer.model.UserAccount;
import com.meridian.transfer.repository.AccountMovementRepository;
import com.meridian.transfer.repository.AppUserRepository;
import com.meridian.transfer.repository.CountryRepository;
import com.meridian.transfer.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

/** The authenticated user's own funding sources — their deposit account plus any cards they've
 *  added. */
@RestController
@RequestMapping("/api/accounts")
public class UserAccountController {

    private final UserAccountRepository userAccountRepository;
    private final AppUserRepository appUserRepository;
    private final CountryRepository countryRepository;
    private final AccountMovementRepository accountMovementRepository;

    public UserAccountController(UserAccountRepository userAccountRepository, AppUserRepository appUserRepository,
                                  CountryRepository countryRepository, AccountMovementRepository accountMovementRepository) {
        this.userAccountRepository = userAccountRepository;
        this.appUserRepository = appUserRepository;
        this.countryRepository = countryRepository;
        this.accountMovementRepository = accountMovementRepository;
    }

    @GetMapping
    public List<UserAccountDto> list(Principal principal) {
        AppUser user = currentUser(principal);
        return userAccountRepository.findByOwnerUser_Id(user.getId()).stream()
                .map(UserAccountDto::from).toList();
    }

    /** Full deposit/withdrawal history for one of the person's own accounts. */
    @GetMapping("/{id}/movements")
    public List<AccountMovementDto> movements(@PathVariable Long id, Principal principal) {
        AppUser user = currentUser(principal);
        userAccountRepository.findByIdAndOwnerUser_Id(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + id));
        return accountMovementRepository.findByAccountIdOrderByCreatedAtDesc(id).stream()
                .map(AccountMovementDto::from).toList();
    }

    /** Adds a credit/debit card — only the last 4 digits and detected network are kept; the full
     *  number is never persisted, and the CVC is used only for this one-time verification step,
     *  then discarded — never written to the database, matching standard card-data-minimization
     *  practice. Once a real payment-gateway integration exists, {@link #verifyCard} is the only
     *  method that needs to change (swap the always-true check for a real API call). */
    @PostMapping("/card")
    public ResponseEntity<?> addCard(@Valid @RequestBody CreateCardRequest request, Principal principal) {
        if (!verifyCard(request)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(java.util.Map.of("error", "Card could not be verified."));
        }

        AppUser user = currentUser(principal);
        String currencyCode = countryRepository.findById(request.countryId())
                .map(Country::getCurrencyCode).orElse(null);

        UserAccount account = new UserAccount();
        account.setOwnerUser(user);
        account.setType(AccountType.BANCAIRE);
        account.setCurrencyCode(currencyCode);
        // Simulated starting balance — in reality checking a card's available credit/funds needs
        // a real card-network integration; this demo just picks a plausible starting point so
        // the send flow can debit it like any other funding source.
        account.setBalance(new java.math.BigDecimal("1000.00"));
        account.setCardHolderName(request.cardHolderName());
        account.setCardLast4(request.cardNumber().substring(request.cardNumber().length() - 4));
        account.setCardNetwork(detectNetwork(request.cardNumber()));
        account.setCardExpiryMonth(request.expiryMonth());
        account.setCardExpiryYear(request.expiryYear());
        account.setCreatedAt(Instant.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(UserAccountDto.from(userAccountRepository.save(account)));
    }

    /**
     * TODO: replace this with a real call to a payment-gateway verification endpoint once one
     * exists, passing request.cvc() and request.cardNumber() to confirm the card is genuine and
     * currently valid. For now, always succeeds — there's no real card network to check against.
     * request.cvc() is used here and nowhere else; it's never assigned to any persisted field.
     */
    private boolean verifyCard(CreateCardRequest request) {
        return true;
    }

    /** Basic issuer-prefix detection — Visa starts with 4, Mastercard with 51-55/2221-2720,
     *  Amex with 34/37. Good enough for a demo; a real integration wouldn't need this at all
     *  since the card network is already known by the payment processor. */
    private String detectNetwork(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return "VISA";
        }
        if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
            return "AMEX";
        }
        int firstTwo = Integer.parseInt(cardNumber.substring(0, 2));
        if (firstTwo >= 51 && firstTwo <= 55) {
            return "MASTERCARD";
        }
        return "OTHER";
    }

    /** Removing a card — the deposit account itself can't be removed this way. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        AppUser user = currentUser(principal);
        userAccountRepository.findByIdAndOwnerUser_Id(id, user.getId()).ifPresent(account -> {
            if (account.getType() == AccountType.BANCAIRE) {
                userAccountRepository.delete(account);
            }
        });
        return ResponseEntity.noContent().build();
    }

    private AppUser currentUser(Principal principal) {
        return appUserRepository.findByUsername(principal.getName()).orElseThrow();
    }
}
