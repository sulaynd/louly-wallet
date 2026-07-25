package com.meridian.transfer.controller;

import com.meridian.transfer.dto.SendMoneyRequest;
import com.meridian.transfer.dto.TransferDto;
import com.meridian.transfer.dto.TransferLimitsDto;
import com.meridian.transfer.model.AppUser;
import com.meridian.transfer.model.Country;
import com.meridian.transfer.model.Transfer;
import com.meridian.transfer.repository.AppUserRepository;
import com.meridian.transfer.repository.CountryRepository;
import com.meridian.transfer.repository.TransferRepository;
import com.meridian.transfer.service.TransferService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class TransferController {

    private final TransferService transferService;
    private final TransferRepository transferRepository;
    private final AppUserRepository appUserRepository;
    private final CountryRepository countryRepository;

    public TransferController(TransferService transferService, TransferRepository transferRepository,
                               AppUserRepository appUserRepository, CountryRepository countryRepository) {
        this.transferService = transferService;
        this.transferRepository = transferRepository;
        this.appUserRepository = appUserRepository;
        this.countryRepository = countryRepository;
    }

    @PostMapping("/api/transfers")
    public ResponseEntity<TransferDto> create(@Valid @RequestBody SendMoneyRequest request, Principal principal) {
        Transfer transfer = transferService.createTransfer(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(TransferDto.from(transfer));
    }

    /** So the frontend can validate against the same cap (in the sender's own currency) without
     *  hardcoding it separately. */
    @GetMapping("/api/transfers/limits")
    public TransferLimitsDto limits(Principal principal) {
        Long countryId = appUserRepository.findByUsername(principal.getName())
                .map(AppUser::getCountry)
                .flatMap(countryRepository::findByName)
                .map(Country::getId)
                .orElse(null);
        var limits = transferService.getLimits(countryId);
        return new TransferLimitsDto(limits.getMinAmount(), limits.getMaxAmount(),
                limits.getDailyMaxAmount(), limits.getMonthlyMaxAmount());
    }

    /** Full transfer history for the authenticated user, most recent first. */
    @GetMapping("/api/transfers")
    public List<TransferDto> history(Principal principal) {
        return transferRepository.findByOwnerUsernameOrderByCreatedAtDesc(principal.getName())
                .stream()
                .map(TransferDto::from)
                .toList();
    }

    @GetMapping("/api/transfers/{id}")
    public TransferDto get(@PathVariable Long id, Principal principal) {
        Transfer transfer = transferRepository.findByIdAndOwnerUsername(id, principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + id));
        return TransferDto.from(transfer);
    }

    @GetMapping("/api/transfers/latest")
    public ResponseEntity<TransferDto> latest(Principal principal) {
        return transferRepository.findTopByOwnerUsernameOrderByCreatedAtDesc(principal.getName())
                .map(t -> ResponseEntity.ok(TransferDto.from(t)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
