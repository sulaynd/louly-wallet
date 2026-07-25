package com.meridian.transfer.repository;

import com.meridian.transfer.model.Recipient;
import com.meridian.transfer.model.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    List<Recipient> findByType(RecipientType type);
    List<Recipient> findByOwnerUsername(String ownerUsername);
    List<Recipient> findByOwnerUsernameAndType(String ownerUsername, RecipientType type);
}
