
//----------------------------------New Final code with the logic to send ml request --------------

/**
 * TransactionService - updated by Tanishq Kathar (29-12-2025)
 * ------------------
 * This class is the CORE ORCHESTRATOR of the transaction flow in AstraGuard.
 *
 * RESPONSIBILITIES:
 * 1. Accepts transaction requests from the app layer (UPI transfer).
 * 2. Authenticates and validates the sender (PIN, balance, receiver existence).
 * 3. Persists the transaction and ledger entries (debit / credit).
 * 4. Assembles ML-ready features using historical + current data.
 * 5. Calls the external ML Risk Engine (Python service).
 * 6. Stores risk scores and fraud decision inside the transaction record.
 * 7. Returns a final decision (ALLOW / FLAG / BLOCK) to the app.
 *
 * HIGH-LEVEL FLOW:
 * App Request
 *   → Validate User & PIN
 *   → Check Balance
 *   → Build Transaction Entity
 *   → Assemble ML Features (FeatureAssemblerService)
 *   → Call ML Risk Engine
 *   → Calculate Final Risk Score
 *   → Apply Fraud Decision Logic
 *   → Persist Transaction + Ledger
 *   → Respond to App
 *
 * WHY THIS CLASS EXISTS:
 * - Acts as the "brain" of the backend.
 * - Keeps controllers thin and logic centralized.
 * - Separates business rules from ML inference and persistence.
 *
 * IMPORTANT NOTES:
 * - ML service is external and can be replaced without changing core logic.
 * - Risk decisions are stored for audit, dashboards, and retraining.
 * - Exceptions stop execution early (BLOCK / FLAG cases).
 *
 * This design mirrors real-world fintech transaction engines.
 */






package com.tksolutions.astraguard.service;

import com.tksolutions.astraguard.dto.TransactionResponse;
import com.tksolutions.astraguard.dto.TransactionTransferRequest;
import com.tksolutions.astraguard.dto.ml.MlRiskRequest;
import com.tksolutions.astraguard.dto.RiskEvaluationResponse;
import com.tksolutions.astraguard.exception.InsufficientBalanceException;
import com.tksolutions.astraguard.exception.InvalidPinException;
import com.tksolutions.astraguard.exception.ReceiverNotFoundException;
import com.tksolutions.astraguard.model.AuthUser;
import com.tksolutions.astraguard.model.entity.*;
import com.tksolutions.astraguard.model.entity.embedded.*;
import com.tksolutions.astraguard.repository.LedgerRepository;
import com.tksolutions.astraguard.repository.TransactionRepository;
import com.tksolutions.astraguard.repository.UserRepository;
import com.tksolutions.astraguard.service.feature.FeatureAssemblerService;
import com.tksolutions.astraguard.utils.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final RiskEngineClientService riskEngineClientService;
    private final FeatureAssemblerService featureAssemblerService;
    private final HttpServletRequest httpServletRequest;

    public TransactionService(
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            LedgerRepository ledgerRepository,
            RiskEngineClientService riskEngineClientService,
            FeatureAssemblerService featureAssemblerService,
            HttpServletRequest httpServletRequest
    ) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.riskEngineClientService = riskEngineClientService;
        this.featureAssemblerService = featureAssemblerService;
        this.httpServletRequest = httpServletRequest;
    }

    public TransactionResponse processTransaction(
            AuthUser authUser,
            TransactionTransferRequest request
    ) {

        // 1️⃣ Load sender
        UserEntity sender = userRepository
                .findById(authUser.getUserId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // 2️⃣ Validate PIN
        if (!PasswordUtil.matches(request.getPin(), sender.getPinHash())) {
            throw new InvalidPinException();
        }

        // 3️⃣ Get sender balance
        long senderBalance =
                ledgerRepository
                        .findTopByUserIdOrderByCreatedAtDesc(sender.getId())
                        .map(LedgerEntryEntity::getBalanceAfter)
                        .orElse(0L);

        if (senderBalance < request.getAmount()) {
            throw new InsufficientBalanceException(senderBalance);
        }

        // 4️⃣ Load receiver
        UserEntity receiver = userRepository
                .findByUpiId(request.getToUpi())
                .orElseThrow(() -> new ReceiverNotFoundException(request.getToUpi()));

        long receiverBalance =
                ledgerRepository
                        .findTopByUserIdOrderByCreatedAtDesc(receiver.getId())
                        .map(LedgerEntryEntity::getBalanceAfter)
                        .orElse(0L);

        // 5️⃣ Build transaction entity (NO DB WRITE YET)
        String txnId = UUID.randomUUID().toString();

        TransactionEntity txn = new TransactionEntity();
        txn.setId(txnId);
        txn.setSenderId(sender.getId());
        txn.setReceiverId(receiver.getId());
        txn.setAmount(request.getAmount());
        txn.setStatus("PENDING");
        txn.setTransactionType(request.getTransactionType());

        DeviceInfo device = new DeviceInfo();
        device.setDeviceId(request.getDevice().getDeviceId());
        device.setDeviceType(request.getDevice().getDeviceType());
        txn.setDevice(device);

        LocationInfo location = new LocationInfo();
        location.setCity(request.getLocation().getCity());
        location.setCountry(request.getLocation().getCountry());
        txn.setLocation(location);

        NetworkInfo network = new NetworkInfo();
        network.setIpAddress(httpServletRequest.getRemoteAddr());
        txn.setNetwork(network);

        txn.setCreatedAt(Instant.now());
        txn.setUpdatedAt(Instant.now());

        // =========================
        // 🔥 ML FEATURE ASSEMBLY
        // =========================

        long totalTxnCount =
                transactionRepository.countBySenderId(sender.getId());

        Optional<TransactionEntity> lastTxn =
                transactionRepository
                        .findTopBySenderIdOrderByCreatedAtDesc(sender.getId());

        MlRiskRequest mlRequest =
                featureAssemblerService.buildMlRiskRequest(
                        txn,
                        sender,
                        receiver,
                        senderBalance,
                        receiverBalance,
                        totalTxnCount,
                        lastTxn
                );

        // =========================
        // 🤖 CALL ML RISK ENGINE
        // =========================

        RiskEvaluationResponse mlResponse =
                riskEngineClientService.evaluateRisk(mlRequest);

        // =========================
        // 🧠 MAP ML → RISK INFO
        // =========================

        double finalRiskScore =
                (mlResponse.getMlScore() * 0.5) +
                        (mlResponse.getRuleScore() * 0.3) +
                        (mlResponse.getAnomalyScore() * 0.2);

        RiskInfo risk = new RiskInfo();
        risk.setRiskScore(finalRiskScore);
        risk.setReasons(mlResponse.getReasons());

        if (finalRiskScore >= 80) {
            risk.setDecision("BLOCK");
            txn.setStatus("BLOCKED");
        } else if (finalRiskScore >= 45) {
            risk.setDecision("FLAG");
            txn.setStatus("FLAGGED");
        } else {
            risk.setDecision("ALLOW");
            txn.setStatus("SUCCESS");
        }

        txn.setRisk(risk);
        System.out.println("Risk Score: " + finalRiskScore);
        // =========================
        // 💾 SAVE TRANSACTION
        // =========================

        transactionRepository.save(txn);

        if (!"SUCCESS".equals(txn.getStatus())) {
            return new TransactionResponse(
                    txnId,
                    txn.getStatus(),
                    finalRiskScore,
                    "Transaction " + txn.getStatus()
            );
        }

        // =========================
        // 💸 LEDGER UPDATES
        // =========================

        senderBalance -= request.getAmount();
        receiverBalance += request.getAmount();

        LedgerEntryEntity debit = new LedgerEntryEntity();
        debit.setId(UUID.randomUUID().toString());
        debit.setTxnId(txnId);
        debit.setUserId(sender.getId());
        debit.setType("DEBIT");
        debit.setAmount(request.getAmount());
        debit.setBalanceAfter(senderBalance);
        debit.setCreatedAt(Instant.now());

        LedgerEntryEntity credit = new LedgerEntryEntity();
        credit.setId(UUID.randomUUID().toString());
        credit.setTxnId(txnId);
        credit.setUserId(receiver.getId());
        credit.setType("CREDIT");
        credit.setAmount(request.getAmount());
        credit.setBalanceAfter(receiverBalance);
        credit.setCreatedAt(Instant.now());

        ledgerRepository.save(debit);
        ledgerRepository.save(credit);

        sender.setCurrentBalance(senderBalance);
        receiver.setCurrentBalance(receiverBalance);

        userRepository.save(sender);
        userRepository.save(receiver);

        // =========================
        // ✅ FINAL RESPONSE
        // =========================

        return new TransactionResponse(
                txnId,
                "SUCCESS",
                finalRiskScore,
                "Transaction completed successfully"
        );
    }
}
