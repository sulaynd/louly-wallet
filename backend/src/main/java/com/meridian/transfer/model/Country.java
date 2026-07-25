package com.meridian.transfer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "countries")
@Getter
@Setter
@NoArgsConstructor
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String flagEmoji;

    private String currencyCode;

    /** E.164 calling code, e.g. "+221" */
    private String callingCode;

    /** Only active countries are offered at registration / add-recipient — toggled via /api/admin/countries. */
    @Column(nullable = false)
    private boolean active = true;
}
