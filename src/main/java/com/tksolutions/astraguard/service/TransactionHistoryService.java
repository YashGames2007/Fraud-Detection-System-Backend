package com.tksolutions.astraguard.service;

import com.tksolutions.astraguard.dto.TransactionHistoryItem;
import com.tksolutions.astraguard.dto.TransactionHistoryResponse;
import com.tksolutions.astraguard.model.AuthUser;
import com.tksolutions.astraguard.model.entity.TransactionEntity;
import com.tksolutions.astraguard.model.entity.UserEntity;
import com.tksolutions.astraguard.repository.TransactionRepository;
import com.tksolutions.astraguard.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionHistoryService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionHistoryService(
            TransactionRepository transactionRepository,
            UserRepository userRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public TransactionHistoryResponse getHistory(AuthUser authUser) {

        List<TransactionEntity> transactions =
                transactionRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(
                        authUser.getUserId(),
                        authUser.getUserId()
                );

        List<TransactionHistoryItem> history =
                transactions.stream()
                        .map(txn -> mapToDto(txn))
                        .collect(Collectors.toList());

        return new TransactionHistoryResponse(history);
    }

    private TransactionHistoryItem mapToDto(TransactionEntity txn) {

        // Always return receiver UPI as toUpi
        UserEntity receiver = userRepository
                .findById(txn.getReceiverId())
                .orElseThrow(); // safe for MVP

        return new TransactionHistoryItem(
                txn.getId(),
                receiver.getUpiId(),
                txn.getAmount(),
                txn.getStatus(),
                txn.getCreatedAt()
        );
    }
}
