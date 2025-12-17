package com.tksolutions.astraguard.dto;

import java.util.List;

public class TransactionHistoryResponse {

    private List<TransactionHistoryItem> transactions;

    public TransactionHistoryResponse(List<TransactionHistoryItem> transactions) {
        this.transactions = transactions;
    }

    public List<TransactionHistoryItem> getTransactions() {
        return transactions;
    }
}
