package com.tksolutions.astraguard.service.feature.impl;

import com.tksolutions.astraguard.dto.ml.MlRiskRequest;
import com.tksolutions.astraguard.model.entity.TransactionEntity;
import com.tksolutions.astraguard.model.entity.UserEntity;
import com.tksolutions.astraguard.service.feature.FeatureAssemblerService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FeatureAssemblerServiceImpl implements FeatureAssemblerService {

    @Override
    public MlRiskRequest buildMlRiskRequest(
            TransactionEntity currentTxn,
            UserEntity sender,
            UserEntity receiver,
            long senderBalanceBefore,
            long receiverBalanceBefore,
            long totalTxnCount,
            Optional<TransactionEntity> lastTxn
    ) {

        MlRiskRequest ml = new MlRiskRequest();

        // 1️⃣ Step logic
        long step = totalTxnCount + 1;
        long prevStep = step-1;

        ml.setStep(step);
        ml.setPrevStep(prevStep);

        // 2️⃣ FIXED transaction type mapping
        ml.setTypeToken(mapTransactionType(currentTxn.getTransactionType()));

        // 3️⃣ Amount (RAW)
        ml.setAmount(currentTxn.getAmount());

        // 4️⃣ Sender features
        ml.setNameOrigToken("TKN_USER_" + sender.getId());
        ml.setOldBalanceOrg(senderBalanceBefore);
        ml.setNewBalanceOrig(senderBalanceBefore - currentTxn.getAmount());

        // 5️⃣ Receiver features
        ml.setNameDestToken("TKN_DEST_" + receiver.getId());
        ml.setOldBalanceDest(receiverBalanceBefore);
        ml.setNewBalanceDest(receiverBalanceBefore + currentTxn.getAmount());

        return ml;
    }

    /**
     * ML-approved transaction type mapping
     */
    private String mapTransactionType(String rawType) {
        return switch (rawType.toUpperCase()) {
            case "PAYMENT"  -> "TYPE_TKN_01";
            case "TRANSFER" -> "TYPE_TKN_02";
            case "CASH_OUT" -> "TYPE_TKN_03";
            case "DEBIT"    -> "TYPE_TKN_04";
            case "CASH_IN"  -> "TYPE_TKN_05";
            default -> throw new IllegalArgumentException(
                    "Unsupported transaction type for ML: " + rawType
            );
        };
    }
}
