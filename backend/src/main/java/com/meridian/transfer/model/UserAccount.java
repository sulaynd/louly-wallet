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

/**
 * A funding source for sending money — a person can have several (one DEPOT, plus any number of
 * BANCAIRE cards). DEPOT accounts are auto-created at registration and have a real balance Louly
 * Express tracks (topped up in person via an agent); BANCAIRE accounts represent a linked
 * credit/debit card, self-added by the person, with a simulated balance (a real card-network
 * integration would be needed to check it for real).
 */
@Entity
@Table(name = "user_accounts")
@Getter
@Setter
@NoArgsConstructor
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Real FK to the account holder — replaces the earlier ownerUsername string field, same
     *  reasoning as receptionMode's partner_id: no ambiguity, referential integrity enforced. */
    @ManyToOne
    @JoinColumn(name = "owner_user_id")
    private AppUser ownerUser;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    /** ISO currency code — matches the holder's own country for a DEPOT account. */
    private String currencyCode;

    /** Tracked for both account types — BANCAIRE's is simulated (a demo starting value), since a
     *  real card-network integration would be needed to check it for real. */
    private BigDecimal balance;

    /** Display label, e.g. "Compte dépôt Louly Express" for DEPOT. Not used for BANCAIRE — its
     *  display ("Visa •••• 1234") is computed from cardNetwork + cardLast4 instead. */
    private String label;

    /** DEPOT only — auto-generated at account creation (e.g. "LE00000123"). Null for BANCAIRE. */
    private String accountNumber;

    /**
     * BANCAIRE only, from here down — never the full card number or CVV, following standard
     * card-data-minimization practice even in this demo. Null for DEPOT.
     */
    private String cardHolderName;

    /** Last 4 digits only. */
    private String cardLast4;

    /** VISA / MASTERCARD / AMEX / OTHER — detected from the card number's leading digits. */
    private String cardNetwork;

    private String cardExpiryMonth;
    private String cardExpiryYear;

    private Instant createdAt;
}
