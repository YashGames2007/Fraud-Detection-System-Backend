package com.tksolutions.astraguard.repository;

import com.tksolutions.astraguard.model.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository
        extends MongoRepository<TransactionEntity, String> {
    List<TransactionEntity> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(
            String senderId,
            String receiverId
    );
}

