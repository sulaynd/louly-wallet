package com.meridian.transfer.repository;

import com.meridian.transfer.model.AccountMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountMovementRepository extends JpaRepository<AccountMovement, Long> {
    List<AccountMovement> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
