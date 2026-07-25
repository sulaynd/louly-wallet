package com.meridian.transfer.model;

public enum MovementType {
    /** Cash added in person via an agent. */
    DEPOSIT,
    /** Money leaving the account — currently only from sending a transfer. */
    WITHDRAWAL
}
