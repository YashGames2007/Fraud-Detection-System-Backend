package com.tksolutions.astraguard.exception;

public class ReceiverNotFoundException extends RuntimeException {
    public ReceiverNotFoundException(String upi) {
        super("Receiver not found: " + upi);
    }
}
