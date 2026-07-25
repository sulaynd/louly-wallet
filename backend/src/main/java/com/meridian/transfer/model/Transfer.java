package com.meridian.transfer.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_id")
    private Recipient recipient;

    @Enumerated(EnumType.STRING)
    private RecipientType mode;

    private BigDecimal amountSent;
    private BigDecimal amountReceived;

    private String sourceCurrency;
    private String targetCurrency;

    private BigDecimal exchangeRate;
    private BigDecimal fee;
    private BigDecimal totalCharged;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    private Instant createdAt;

    /** Username of the account that created this transfer — keeps transfer history per-user. */
    private String ownerUsername;

    /** Which of the sender's own accounts funded this transfer — snapshotted (type + label) at
     *  the moment of sending, not a live FK, so it survives even if a bank-account reference is
     *  later removed. */
    @Enumerated(EnumType.STRING)
    private AccountType sourceAccountType;
    private String sourceAccountLabel;

    /** Set only when funded from a BANCAIRE account — the simulated bank's authorization
     *  reference code, for the same traceability a real payment-gateway integration would need. */
    private String bankAuthorizationReference;

    /**
     * Commission snapshots, frozen at transaction time (same principle as rate/fee) so later
     * commission-rate changes never rewrite what was actually earned on a past transaction.
     */
    private BigDecimal platformCommissionAmount;
    private String platformCommissionCurrency;
    private BigDecimal receivingReceptionModeCommissionAmount;
    private String receivingReceptionModeCommissionCurrency;

    /** Which commission-rate bucket applied, and the total rate (%) used — for traceability. */
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    private BigDecimal commissionRatePercent;

    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("occurredAt asc")
    @JsonManagedReference
    private List<TransferEvent> events = new ArrayList<>();
}
