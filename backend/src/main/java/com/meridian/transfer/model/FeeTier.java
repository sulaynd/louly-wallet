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

/**
 * The customer-facing transfer fee, tiered by amount sent — e.g. $1-$100 → 3%, $101-$300 → 4%.
 * Tiers are per-country and denominated in that country's own currency (min/max amounts mean
 * nothing across currencies otherwise) — a Senegalese sender's tiers are in XOF, a Canadian
 * sender's are in CAD, etc. This avoids depending on live exchange rates just to figure out
 * which bracket a transfer falls into.
 * <p>
 * This IS the total commission recorded for a transaction (see TransferService.applyCommission):
 * what the client is charged always exactly matches what accounting records as earned, so there's
 * never a payout owed to a receiving reception mode that wasn't actually collected from the client.
 * {@code maxAmount} null means open-ended (no upper bound on this tier).
 */
@Entity
@Table(name = "fee_tiers")
@Getter
@Setter
@NoArgsConstructor
public class FeeTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    private BigDecimal minAmount;

    /** Null = open-ended (no upper bound). */
    private BigDecimal maxAmount;

    private BigDecimal feePercent;
}
