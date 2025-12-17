package com.tksolutions.astraguard.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(long available) {
        super("Insufficient balance. Available: " + available);
    }
}
