package com.meridian.transfer.controller;

import com.meridian.transfer.dto.LoginRequest;
import com.meridian.transfer.dto.RegisterRequest;
import com.meridian.transfer.model.AppUser;
import com.meridian.transfer.model.Country;
import com.meridian.transfer.model.AccountType;
import com.meridian.transfer.model.UserAccount;
import com.meridian.transfer.repository.AppUserRepository;
import com.meridian.transfer.repository.CountryRepository;
import com.meridian.transfer.repository.UserAccountRepository;
import com.meridian.transfer.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final CountryRepository countryRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AppUserRepository appUserRepository,
                           CountryRepository countryRepository,
                           UserAccountRepository userAccountRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.countryRepository = countryRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username already taken"));
        }

        Optional<Country> country = countryRepository.findByName(request.country());
        if (country.isEmpty() || !country.get().isActive()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown or currently unsupported country."));
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName() != null && !request.displayName().isBlank()
                ? request.displayName() : request.username());
        user.setPhoneNumber(request.phoneNumber());
        user.setCountry(request.country());
        user.setFlagEmoji(country.get().getFlagEmoji());
        appUserRepository.save(user);

        // Every account gets a Louly Express deposit account automatically — no separate
        // enrollment step. Balance starts at zero; an agent tops it up in person later.
        UserAccount depot = new UserAccount();
        depot.setOwnerUser(user);
        depot.setType(AccountType.DEPOT);
        depot.setCurrencyCode(country.get().getCurrencyCode());
        depot.setBalance(BigDecimal.ZERO);
        depot.setLabel("Compte dépôt Louly Express");
        depot.setCreatedAt(Instant.now());
        userAccountRepository.save(depot);
        // The account number embeds the row's own ID for uniqueness — generated after the first
        // save so the ID actually exists, then persisted with a second save.
        depot.setAccountNumber("LE" + String.format("%08d", depot.getId()));
        userAccountRepository.save(depot);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", user.getUsername()));
    }

    /** Validates the password and returns a JWT, plus the profile info the frontend needs. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        }

        AppUser user = appUserRepository.findByUsername(request.username()).orElseThrow();
        String token = jwtService.generateToken(user.getUsername(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "displayName", user.getDisplayName() == null ? "" : user.getDisplayName(),
                "phoneNumber", user.getPhoneNumber() == null ? "" : user.getPhoneNumber(),
                "country", user.getCountry() == null ? "" : user.getCountry(),
                "flagEmoji", user.getFlagEmoji() == null ? "" : user.getFlagEmoji(),
                "role", user.getRole()
        ));
    }

    /** Confirms the current token is still valid and returns fresh profile info. */
    @GetMapping("/me")
    public Map<String, String> me(Principal principal) {
        AppUser user = appUserRepository.findByUsername(principal.getName()).orElseThrow();
        return Map.of(
                "username", user.getUsername(),
                "displayName", user.getDisplayName() == null ? "" : user.getDisplayName(),
                "phoneNumber", user.getPhoneNumber() == null ? "" : user.getPhoneNumber(),
                "country", user.getCountry() == null ? "" : user.getCountry(),
                "flagEmoji", user.getFlagEmoji() == null ? "" : user.getFlagEmoji()
        );
    }
}
