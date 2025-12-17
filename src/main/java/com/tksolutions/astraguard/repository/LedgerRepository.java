package com.tksolutions.astraguard.repository;

import com.tksolutions.astraguard.model.entity.LedgerEntryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LedgerRepository
        extends MongoRepository<LedgerEntryEntity, String> {
    Optional<LedgerEntryEntity>
    findTopByUserIdOrderByCreatedAtDesc(String userId);

}
