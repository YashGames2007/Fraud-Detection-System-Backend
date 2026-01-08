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

    public TransactionResponse processTransaction(AuthUser authUser, TransactionTransferRequest request) {
        System.out.println("DEBUG: Starting transaction for user: " + authUser.getUserId());

        // 1️⃣ Load sender
        UserEntity sender = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        System.out.println("DEBUG: Sender found: " + sender.getUpiId());

        // 2️⃣ Validate PIN
        if (!PasswordUtil.matches(request.getPin(), sender.getPinHash())) {
            System.out.println("DEBUG: PIN Validation Failed");
            throw new InvalidPinException();
        }

        // 3️⃣ Fetch sender balance
        long senderBalance = ledgerRepository.findTopByUserIdOrderByCreatedAtDesc(sender.getId())
                .map(LedgerEntryEntity::getBalanceAfter)
                .orElse(0L);
        // Check if sender has enough money
        if (senderBalance < request.getAmount()) {
            System.out.println("DEBUG: Transaction failed due to insufficient balance."); //
            throw new InsufficientBalanceException(senderBalance); // This stops the program and sends the error to the app
        }

        // 4️⃣ Load receiver
        UserEntity receiver = userRepository.findByUpiId(request.getToUpi())
                .orElseThrow(() -> new ReceiverNotFoundException(request.getToUpi()));
        System.out.println("DEBUG: Receiver found: " + receiver.getUpiId());

        long receiverBalance = ledgerRepository.findTopByUserIdOrderByCreatedAtDesc(receiver.getId())
                .map(LedgerEntryEntity::getBalanceAfter)
                .orElse(0L);

        // 5️⃣ Build transaction entity
        String txnId = UUID.randomUUID().toString();
        TransactionEntity txn = new TransactionEntity();
        txn.setId(txnId);
        txn.setSenderId(sender.getId());
        txn.setReceiverId(receiver.getId());
        txn.setAmount(request.getAmount());
        txn.setStatus("PENDING");
        txn.setTransactionType(request.getTransactionType());
        txn.setCreatedAt(Instant.now());
        txn.setUpdatedAt(Instant.now());

        // Location/Device/Network setup...
        LocationInfo location = new LocationInfo();
        location.setCity(request.getLocation().getCity());
        location.setCountry(request.getLocation().getCountry());
        txn.setLocation(location);

        DeviceInfo device = new DeviceInfo();
        device.setDeviceId(request.getDevice().getDeviceId());
        device.setDeviceType(request.getDevice().getDeviceType());
        txn.setDevice(device);

        NetworkInfo network = new NetworkInfo();
        network.setIpAddress(httpServletRequest.getRemoteAddr());
        txn.setNetwork(network);

        // =========================
        //ML FEATURE ASSEMBLY

        System.out.println("DEBUG: Assembling ML Features...");
        long totalTxnCount = transactionRepository.countBySenderId(sender.getId());
        Optional<TransactionEntity> lastTxn = transactionRepository.findTopBySenderIdOrderByCreatedAtDesc(sender.getId());

        MlRiskRequest mlRequest = featureAssemblerService.buildMlRiskRequest(
                txn, sender, receiver, senderBalance, receiverBalance, totalTxnCount, lastTxn);

        System.out.println("DEBUG: ML Request Payload: " + mlRequest.toString());

        // =========================
        // CALL ML ENGINE

        System.out.println("DEBUG: Calling ML Risk Engine at Hugging Face...");
        RiskEvaluationResponse mlResponse;

        try {
            mlResponse = riskEngineClientService.evaluateRisk(mlRequest);
            System.out.println("DEBUG: ML Response received: " + mlResponse);
        } catch (Exception e) {
            System.out.println("ERROR: ML Engine Call Failed! Reason: " + e.getMessage());
            throw new RuntimeException("ML Risk Engine is unavailable. Try again later.");
        }

        // Safety check to prevent NullPointerException
        if (mlResponse == null || mlResponse.getRiskScore() == null) {
            System.out.println("ERROR: ML Response is NULL or RiskScore is NULL");
            throw new RuntimeException("Invalid response from Risk Engine");
        }

        //=========================
        //RISK DECISION

        double finalRiskScore = mlResponse.getRiskScore();
        System.out.println("DEBUG: Final Risk Score: " + finalRiskScore);

        RiskInfo risk = new RiskInfo();
        risk.setRiskScore(finalRiskScore);

        if (finalRiskScore >= 0.8) {
            risk.setDecision("BLOCK");
            txn.setStatus("BLOCKED");
        } else if (finalRiskScore >= 0.45) {
            risk.setDecision("FLAG");
            txn.setStatus("FLAGGED");
        } else {
            risk.setDecision("ALLOW");
            txn.setStatus("SUCCESS");
        }

        risk.setReasons(mlResponse.getReasons() != null ? mlResponse.getReasons() : List.of("ML Analysis completed"));
        txn.setRisk(risk);
        transactionRepository.save(txn);

        if (!"SUCCESS".equals(txn.getStatus())) {
            System.out.println("DEBUG: Transaction " + txn.getStatus() + " based on score.");
            return new TransactionResponse(txnId, txn.getStatus(), finalRiskScore, "Transaction " + txn.getStatus());
        }

        // =========================
        //LEDGER ENTRIES

        System.out.println("DEBUG: Updating Ledgers and Balances...");
        long newSenderBalance = senderBalance - request.getAmount();
        long newReceiverBalance = receiverBalance + request.getAmount();

        // (Ledger Save Logic stays same...)
        LedgerEntryEntity debit = new LedgerEntryEntity();
        debit.setId(UUID.randomUUID().toString());
        debit.setTxnId(txnId);
        debit.setUserId(sender.getId());
        debit.setType("DEBIT");
        debit.setAmount(request.getAmount());
        debit.setBalanceAfter(newSenderBalance);
        debit.setCreatedAt(Instant.now());
        ledgerRepository.save(debit);

        LedgerEntryEntity credit = new LedgerEntryEntity();
        credit.setId(UUID.randomUUID().toString());
        credit.setTxnId(txnId);
        credit.setUserId(receiver.getId());
        credit.setType("CREDIT");
        credit.setAmount(request.getAmount());
        credit.setBalanceAfter(newReceiverBalance);
        credit.setCreatedAt(Instant.now());
        ledgerRepository.save(credit);

        sender.setCurrentBalance(newSenderBalance);
        receiver.setCurrentBalance(newReceiverBalance);
        userRepository.save(sender);
        userRepository.save(receiver);

        System.out.println("DEBUG: Transaction COMPLETED Successfully.");
        return new TransactionResponse(txnId, "SUCCESS", finalRiskScore, "Transaction completed successfully");
    }
}