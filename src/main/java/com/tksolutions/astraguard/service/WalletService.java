package com.tksolutions.astraguard.service;

import com.tksolutions.astraguard.dto.BalanceResponse;
import com.tksolutions.astraguard.model.AuthUser;
import com.tksolutions.astraguard.model.entity.LedgerEntryEntity;
import com.tksolutions.astraguard.repository.LedgerRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final LedgerRepository ledgerRepository;

    public WalletService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    public BalanceResponse getBalance(AuthUser authUser) {

        long balance = ledgerRepository
                .findTopByUserIdOrderByCreatedAtDesc(authUser.getUserId())
                .map(LedgerEntryEntity::getBalanceAfter)
                .orElse(0L);

        return new BalanceResponse(balance);
    }
}
