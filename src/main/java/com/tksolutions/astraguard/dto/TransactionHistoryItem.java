package com.tksolutions.astraguard.dto;

import java.time.Instant;

public class TransactionHistoryItem {

    private String transactionId;
    private String toUpi;
    private long amount;
    private String status;
    private Instant createdAt;

    public TransactionHistoryItem(
            String transactionId,
            String toUpi,
            long amount,
            String status,
            Instant createdAt
    ) {
        this.transactionId = transactionId;
        this.toUpi = toUpi;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getToUpi() {
        return toUpi;
    }

    public long getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
