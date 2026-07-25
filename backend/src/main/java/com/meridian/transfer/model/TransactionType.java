package com.meridian.transfer.model;

public enum TransactionType {
    /** Domestic peer-to-peer transfer. */
    P2P_LOCAL,
    /** Cash withdrawal through a physical agent. */
    CASH_OUT_AGENT,
    /** Merchant payment via QR code (not yet wired to an actual transfer flow). */
    MERCHANT_QR_PAYMENT,
    /** Incoming international transfer, e.g. from the diaspora (not yet wired to an actual flow). */
    INTERNATIONAL_INBOUND,
    /** Outgoing international transfer with currency exchange — this app's international sends. */
    INTERNATIONAL_OUTBOUND_FX
}
