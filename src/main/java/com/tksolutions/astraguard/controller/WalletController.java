package com.tksolutions.astraguard.controller;

import com.tksolutions.astraguard.dto.BalanceResponse;
import com.tksolutions.astraguard.model.AuthUser;
import com.tksolutions.astraguard.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance() {

        AuthUser authUser = (AuthUser)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        BalanceResponse response = walletService.getBalance(authUser);

        return ResponseEntity.ok(response);
    }
}
