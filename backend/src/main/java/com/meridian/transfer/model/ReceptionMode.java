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

@Entity
@Table(name = "reception_modes")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    /** e.g. "Wave ou Orange", "BNB Cash Pickup", "Compte bancaire" */
    private String name;

    /** Short explanation shown under the name, in each language. */
    private String descriptionFr;
    private String descriptionEn;

    /** Whether this receptionMode can be used for actual delivery (shows up in "Partenaire de
     *  livraison"). Toggled by customer service via /api/admin/reception-modes. */
    private boolean livrable = false;

    /** Whether this receptionMode shows up as a reception-mode choice for its country. Toggled by
     *  customer service via /api/admin/reception-modes — a deactivated receptionMode disappears from the
     *  "mode de réception" chips without deleting its history. */
    private boolean active = true;
}
