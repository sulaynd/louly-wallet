package com.meridian.transfer.model;

import jakarta.persistence.Entity;
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

/** One row per country — min/max are denominated in that country's own currency. This is the
 *  single source of truth for transaction-size limits (a business/compliance concern); the
 *  separate fee_tiers grid handles pricing only, no longer transaction bounds. */
@Entity
@Table(name = "transfer_limits")
@Getter
@Setter
@NoArgsConstructor
public class TransferLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    /** Cumulative caps — sum of everything the account has sent in the period, not just this
     *  one transaction. Null means no cumulative cap enforced for that period. */
    private BigDecimal dailyMaxAmount;
    private BigDecimal monthlyMaxAmount;

    private Instant updatedAt;

    /** Username of the customer-service account that last changed it, for an audit trail. */
    private String updatedBy;
}
