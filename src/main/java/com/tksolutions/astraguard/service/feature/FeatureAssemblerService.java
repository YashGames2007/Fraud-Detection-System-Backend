package com.tksolutions.astraguard.service.feature;

import com.tksolutions.astraguard.dto.ml.MlRiskRequest;
import com.tksolutions.astraguard.model.entity.TransactionEntity;
import com.tksolutions.astraguard.model.entity.UserEntity;

import java.util.Optional;

public interface FeatureAssemblerService {

    MlRiskRequest buildMlRiskRequest(
            TransactionEntity currentTxn,
            UserEntity sender,
            UserEntity receiver,
            long senderBalanceBefore,
            long receiverBalanceBefore,
            long totalTxnCount,
            Optional<TransactionEntity> lastTxn
    );
}
