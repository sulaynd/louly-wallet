package com.meridian.transfer.model;

public enum LedgerEntryType {
    /** Auto-created on every transfer — increases what Louly Express owes the receiving receptionMode. */
    COMMISSION_OWED,
    /** Manually recorded by customer service when Louly Express actually pays a receptionMode. */
    SETTLEMENT_PAYMENT
}
