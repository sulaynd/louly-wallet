package com.meridian.transfer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * How the total commission (now equal to the customer-facing fee — see FeeTierService) is split
 * for a given transaction type: {@code partnerSharePercent} goes to the receiving reception mode
 * (e.g. Wave ou Orange), the rest is Louly Express's platform revenue. Editable by customer
 * service via /api/admin/commission-rates.
 */
@Entity
@Table(name = "commission_rates")
@Getter
@Setter
@NoArgsConstructor
public class CommissionRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    /** Human-readable label, e.g. "Transfert P2P local" */
    private String label;

    /** % of the total commission that goes to the receiving reception mode; the rest is platform revenue. */
    private BigDecimal partnerSharePercent;
}
