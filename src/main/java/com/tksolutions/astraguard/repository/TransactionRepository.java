package com.tksolutions.astraguard.repository;

import com.tksolutions.astraguard.model.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends MongoRepository<TransactionEntity, String> {
    List<TransactionEntity> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(
            String senderId,
            String receiverId
    );

    long countBySenderId(String id);

    Optional<TransactionEntity> findTopBySenderIdOrderByCreatedAtDesc(String id);
}

