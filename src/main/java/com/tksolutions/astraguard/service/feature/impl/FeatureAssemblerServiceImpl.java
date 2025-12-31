    package com.tksolutions.astraguard.service.feature.impl;

    import com.tksolutions.astraguard.dto.ml.MlRiskRequest;
    import com.tksolutions.astraguard.model.entity.TransactionEntity;
    import com.tksolutions.astraguard.model.entity.UserEntity;
    import com.tksolutions.astraguard.service.feature.FeatureAssemblerService;
    import org.springframework.stereotype.Service;

    import java.nio.charset.StandardCharsets;
    import java.security.MessageDigest;
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
            long prevStep = lastTxn
                    .map(txn -> txn.getRisk() != null ? step - 1 : step - 1)
                    .orElse(step - 1);

            ml.setStep(step);
            ml.setPrevStep(prevStep);

            // 2️⃣ Transaction type token
            ml.setTypeToken(tokenize("TYPE", currentTxn.getTransactionType()));

            // 3️⃣ Amount (raw)
            ml.setAmount(currentTxn.getAmount());

            // 4️⃣ Sender features
            ml.setNameOrigToken(tokenize("USER", sender.getUpiId()));
            ml.setOldBalanceOrg(senderBalanceBefore);
            ml.setNewBalanceOrig(senderBalanceBefore - currentTxn.getAmount());

            // 5️⃣ Receiver features
            ml.setNameDestToken(tokenize("USER", receiver.getUpiId()));
            ml.setOldBalanceDest(receiverBalanceBefore);
            ml.setNewBalanceDest(receiverBalanceBefore + currentTxn.getAmount());

            return ml;
        }

        /**
         * Deterministic tokenization using SHA-256
         */
        private String tokenize(String prefix, String value) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

                // Shorten hash for readability (ML doesn’t need full hash)
                StringBuilder hex = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    hex.append(String.format("%02x", hash[i]));
                }

                return prefix + "_TKN_" + hex;

            } catch (Exception e) {
                throw new RuntimeException("Tokenization failed", e);
            }
        }
    }
