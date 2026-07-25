package com.meridian.transfer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reception_mode_ledger_entries")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionModeLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reception_mode_id")
    private ReceptionMode receptionMode;

    /** The transfer that generated this entry — null for a manual settlement. */
    @ManyToOne
    @JoinColumn(name = "transfer_id")
    private Transfer transfer;

    @Enumerated(EnumType.STRING)
    private LedgerEntryType type;

    private BigDecimal amount;
    private String currency;

    private Instant createdAt;

    private String note;

    /** Username of the admin who recorded a manual settlement — null for auto entries. */
    private String recordedBy;
}
