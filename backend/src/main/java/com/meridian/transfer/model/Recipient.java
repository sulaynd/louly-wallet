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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recipients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private RecipientType type;

    /** e.g. "RBC •••• 4471" or "BDO Bank, Manila" */
    private String detail;

    private String flagEmoji;

    /** ISO currency code the recipient is paid in, e.g. CAD, PHP, INR, EUR */
    private String currencyCode;

    /** E.164-style phone number, e.g. "+221 78 149 90 51" */
    private String phoneNumber;

    /** Display cache only — e.g. "Wave ou Orange". Not authoritative; see receivingReceptionMode. */
    private String receptionModeName;

    /** The actual FK — authoritative source of truth for which receptionMode receives on this
     *  recipient's behalf. Resolves the ambiguity that receptionModeName alone can't (e.g. "Compte
     *  bancaire" exists once per country). */
    @ManyToOne
    @JoinColumn(name = "reception_mode_id")
    private ReceptionMode receivingReceptionMode;

    /** Which receptionMode actually delivers the funds, e.g. "Louly Express", "Orange Money", "Wave" */
    private String deliveryPartner;

    private String address;

    private String city;

    /** Username of the account this recipient belongs to — recipients are private per account. */
    private String ownerUsername;
}
