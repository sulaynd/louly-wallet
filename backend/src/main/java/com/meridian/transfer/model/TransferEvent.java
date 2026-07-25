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

import java.time.Instant;

@Entity
@Table(name = "transfer_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transfer_id")
    private Transfer transfer;

    /** What kind of step this is — the frontend translates the label from this, not from title/subtitle. */
    @Enumerated(EnumType.STRING)
    private TransferEventType type;

    /** English fallback text, kept for API completeness/logging — the UI no longer displays these directly. */
    private String title;
    private String subtitle;

    private Instant occurredAt;

    private boolean pending;
}
