package com.tksolutions.astraguard.exception;

public class InvalidPinException extends RuntimeException {
    public InvalidPinException() {
        super("Invalid PIN");
    }
}

