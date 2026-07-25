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

/** Every balance-changing event on a DEPOT account — deposits (in person, via an agent) and
 *  withdrawals (sending a transfer) — replaces the earlier deposit-only ledger (account_deposits)
 *  with one unified history, same principle as the commission ledger. */
@Entity
@Table(name = "account_movements")
@Getter
@Setter
@NoArgsConstructor
public class AccountMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserAccount account;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    private BigDecimal amount;

    /** Snapshot of the balance right after this movement — makes the history self-explanatory
     *  without having to replay every prior movement. */
    private BigDecimal balanceAfter;

    /** Set only for a WITHDRAWAL caused by sending money. */
    @ManyToOne
    @JoinColumn(name = "related_transfer_id")
    private Transfer relatedTransfer;

    /** Set only for a DEPOSIT — the agent/support account who processed it. */
    @ManyToOne
    @JoinColumn(name = "processed_by_user_id")
    private AppUser processedByUser;

    private Instant createdAt;

    private String note;
}
