package com.meridian.transfer.repository;

import com.meridian.transfer.model.AccountType;
import com.meridian.transfer.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    List<UserAccount> findByOwnerUser_Id(Long ownerUserId);
    Optional<UserAccount> findByIdAndOwnerUser_Id(Long id, Long ownerUserId);
    Optional<UserAccount> findFirstByOwnerUser_IdAndType(Long ownerUserId, AccountType type);
}
