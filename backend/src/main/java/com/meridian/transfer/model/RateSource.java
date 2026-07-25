package com.meridian.transfer.model;

public enum RateSource {
    /** Loaded from data.sql when the row didn't exist yet. */
    SEED,
    /** Fetched automatically from the live provider (Frankfurter). */
    LIVE_PROVIDER,
    /** Set by a customer-service upload or manual edit. */
    MANUAL
}
