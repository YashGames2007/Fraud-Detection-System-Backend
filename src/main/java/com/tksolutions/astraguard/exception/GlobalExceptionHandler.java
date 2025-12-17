package com.tksolutions.astraguard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🔴 External dependency failure
    @ExceptionHandler(RiskServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleRiskServiceDown(
            RiskServiceUnavailableException ex) {

        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage(),
                "RISK_SERVICE_UNAVAILABLE"
        );

        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // 🔴 Invalid transaction request (generic)
    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTransaction(
            InvalidTransactionException ex) {

        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage(),
                "INVALID_TRANSACTION"
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 🔴 Invalid PIN
    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPin(
            InvalidPinException ex) {

        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage(),
                "INVALID_PIN"
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // 🔴 Receiver not found
    @ExceptionHandler(ReceiverNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleReceiverNotFound(
            ReceiverNotFoundException ex) {

        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage(),
                "RECEIVER_NOT_FOUND"
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 🔴 Insufficient balance
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex) {

        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage(),
                "INSUFFICIENT_BALANCE"
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 🔴 Auth / user issues
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
            UserNotFoundException ex) {

        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage(),
                "USER_NOT_FOUND"
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // 🔴 Fallback (DO NOT REMOVE)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex) {

        ApiErrorResponse error = new ApiErrorResponse(
                "Internal server error",
                "INTERNAL_ERROR"
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
