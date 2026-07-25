package com.meridian.transfer.service;

public class TransferLimitExceededException extends RuntimeException {
    public TransferLimitExceededException(String message) {
        super(message);
    }
}
