package com.meridian.transfer.model;

public enum AccountType {
    /** Held and managed by Louly Express itself — has a real balance, toppable up in person via
     *  an agent. */
    DEPOT,
    /** An external bank account, managed by an independent bank — Louly Express has no authority
     *  over it and doesn't track a balance for it; it's just a reference. */
    BANCAIRE
}
