package com.tksolutions.astraguard.repository;

import com.tksolutions.astraguard.model.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository
        extends MongoRepository<TransactionEntity, String> {
}

