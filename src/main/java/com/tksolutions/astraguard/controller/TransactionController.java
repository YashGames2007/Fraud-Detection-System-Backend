package com.tksolutions.astraguard.controller;

import com.tksolutions.astraguard.dto.TransactionHistoryResponse;
import com.tksolutions.astraguard.dto.TransactionRequest;
import com.tksolutions.astraguard.dto.TransactionResponse;
import com.tksolutions.astraguard.dto.TransactionTransferRequest;
import com.tksolutions.astraguard.model.AuthUser;
import com.tksolutions.astraguard.service.TransactionHistoryService;
import com.tksolutions.astraguard.service.TransactionService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionHistoryService transactionHistoryService;

    public TransactionController(
            TransactionService transactionService,
            TransactionHistoryService transactionHistoryService
    ) {
        this.transactionService = transactionService;
        this.transactionHistoryService = transactionHistoryService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionTransferRequest request) {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = auth.getPrincipal();

        System.out.println("Principal class: " + principal.getClass());

        AuthUser user = (AuthUser) auth.getPrincipal();

        System.out.println("Sending Auth User to Process Request");

        TransactionResponse response =
                transactionService.processTransaction(user, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory() {

        AuthUser authUser = (AuthUser)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        TransactionHistoryResponse response =
                transactionHistoryService.getHistory(authUser);

        return ResponseEntity.ok(response);
    }

}
