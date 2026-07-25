package com.meridian.transfer.controller;

import com.meridian.transfer.dto.CreateDepositRequest;
import com.meridian.transfer.dto.DepositDto;
import com.meridian.transfer.model.AccountMovement;
import com.meridian.transfer.model.AccountType;
import com.meridian.transfer.model.AppUser;
import com.meridian.transfer.model.MovementType;
import com.meridian.transfer.model.UserAccount;
import com.meridian.transfer.repository.AccountMovementRepository;
import com.meridian.transfer.repository.AppUserRepository;
import com.meridian.transfer.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;

/** Agent or customer-service only (ROLE_AGENT or ROLE_ADMIN) — see SecurityConfig. Records an
 *  in-person cash deposit into a customer's DEPOT account. */
@RestController
@RequestMapping("/api/agent/deposits")
public class AgentDepositController {

    private final UserAccountRepository userAccountRepository;
    private final AppUserRepository appUserRepository;
    private final AccountMovementRepository accountMovementRepository;

    public AgentDepositController(UserAccountRepository userAccountRepository,
                                   AppUserRepository appUserRepository,
                                   AccountMovementRepository accountMovementRepository) {
        this.userAccountRepository = userAccountRepository;
        this.appUserRepository = appUserRepository;
        this.accountMovementRepository = accountMovementRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateDepositRequest request, Principal principal) {
        AppUser customer = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new EntityNotFoundException("No user: " + request.username()));
        UserAccount account = userAccountRepository.findFirstByOwnerUser_IdAndType(customer.getId(), AccountType.DEPOT)
                .orElseThrow(() -> new EntityNotFoundException("No deposit account for user: " + request.username()));
        AppUser agent = appUserRepository.findByUsername(principal.getName()).orElseThrow();

        account.setBalance(account.getBalance().add(request.amount()));
        userAccountRepository.save(account);

        AccountMovement movement = new AccountMovement();
        movement.setAccount(account);
        movement.setType(MovementType.DEPOSIT);
        movement.setAmount(request.amount());
        movement.setBalanceAfter(account.getBalance());
        movement.setProcessedByUser(agent);
        movement.setCreatedAt(Instant.now());
        movement.setNote(request.note());

        return ResponseEntity.status(HttpStatus.CREATED).body(DepositDto.from(accountMovementRepository.save(movement)));
    }
}
