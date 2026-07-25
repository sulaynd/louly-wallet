package com.meridian.transfer.repository;

import com.meridian.transfer.model.ReceptionModeLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceptionModeLedgerEntryRepository extends JpaRepository<ReceptionModeLedgerEntry, Long> {
    List<ReceptionModeLedgerEntry> findByReceptionModeIdOrderByCreatedAtDesc(Long receptionModeId);
    List<ReceptionModeLedgerEntry> findAllByOrderByCreatedAtDesc();
}
