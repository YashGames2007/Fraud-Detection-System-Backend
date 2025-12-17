package com.tksolutions.astraguard.service;

import com.tksolutions.astraguard.dto.*;
import com.tksolutions.astraguard.exception.InsufficientBalanceException;
import com.tksolutions.astraguard.exception.InvalidPinException;
import com.tksolutions.astraguard.exception.ReceiverNotFoundException;
import com.tksolutions.astraguard.model.AuthUser;
import com.tksolutions.astraguard.model.entity.LedgerEntryEntity;
import com.tksolutions.astraguard.model.entity.TransactionEntity;
import com.tksolutions.astraguard.model.entity.UserEntity;
import com.tksolutions.astraguard.model.entity.embedded.DeviceInfo;
import com.tksolutions.astraguard.model.entity.embedded.LocationInfo;
import com.tksolutions.astraguard.model.entity.embedded.NetworkInfo;
import com.tksolutions.astraguard.model.entity.embedded.RiskInfo;
import com.tksolutions.astraguard.repository.LedgerRepository;
import com.tksolutions.astraguard.repository.TransactionRepository;
import com.tksolutions.astraguard.repository.UserRepository;
import com.tksolutions.astraguard.utils.PasswordUtil;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final RiskEngineClientService riskEngineClientService;
    private final DecisionAgentClientService decisionAgentClientService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final HttpServletRequest httpServletRequest;


    public TransactionService(
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            LedgerRepository ledgerRepository,
            RiskEngineClientService riskEngineClientService,
            DecisionAgentClientService decisionAgentClientService,
            HttpServletRequest httpServletRequest
    ) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.riskEngineClientService = riskEngineClientService;
        this.decisionAgentClientService = decisionAgentClientService;
        this.httpServletRequest = httpServletRequest;
    }

    public TransactionResponse processTransaction(AuthUser authUser, TransactionTransferRequest request) {

        // 1️⃣ Generate transaction reference
//        String transactionId = UUID.randomUUID().toString();

        System.out.println("Generated Transaction: " + request + " User: " + authUser.getUpiId());

        UserEntity sender = userRepository
                .findById(authUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!PasswordUtil.matches(request.getPin(), sender.getPinHash())) {
            throw new InvalidPinException();
        }

        long senderBalance = ledgerRepository
                .findTopByUserIdOrderByCreatedAtDesc(sender.getId())
                .map(LedgerEntryEntity::getBalanceAfter)
                .orElse(0L);

        if (senderBalance < request.getAmount()) {
            throw new InsufficientBalanceException(senderBalance);
        }

//        if (sender.getCurrentBalance() < request.amount) {
//            throw new RuntimeException("Insufficient balance Available: " + sender.getCurrentBalance() + " Requested: " + request.amount);
//        }

        UserEntity receiver = userRepository
                .findByUpiId(request.getToUpi())
                .orElseThrow(() -> new ReceiverNotFoundException(request.getToUpi()));

        String txnId = UUID.randomUUID().toString();


        String clientIp = httpServletRequest.getRemoteAddr();

        // Build transaction entity
        TransactionEntity txn = new TransactionEntity();
        txn.setId(txnId);
        txn.setSenderId(sender.getId());
        txn.setReceiverId(receiver.getId());
        txn.setAmount(request.getAmount());
        txn.setStatus("SUCCESS");
        txn.setTransactionType(request.getTransactionType());

        // Device
        DeviceInfo device = new DeviceInfo();
        device.setDeviceId(request.getDevice().getDeviceId());
        device.setDeviceType(request.getDevice().getDeviceType());
        txn.setDevice(device);

        // Location
        LocationInfo location = new LocationInfo();
        location.setCity(request.getLocation().getCity());
        location.setCountry(request.getLocation().getCountry());
        txn.setLocation(location);

        // Network (server-derived)
        NetworkInfo network = new NetworkInfo();
        network.setIpAddress(clientIp);
        txn.setNetwork(network);

        // Risk (MVP stub for now)
        RiskInfo risk = new RiskInfo();
        risk.setRiskScore(20.0);
        risk.setDecision("ALLOW");
        risk.setReasons(List.of());
        txn.setRisk(risk);

        txn.setCreatedAt(Instant.now());
        txn.setUpdatedAt(Instant.now());

        // SAVE TRANSACTION
        transactionRepository.save(txn);

        // Debit sender
//        long senderBalance = sender.getCurrentBalance() - request.amount;
        senderBalance -=  request.getAmount();
        sender.setCurrentBalance(senderBalance);

        LedgerEntryEntity debit = new LedgerEntryEntity();
        debit.setId(UUID.randomUUID().toString());
        debit.setTxnId(txnId);
        debit.setUserId(sender.getId());
        debit.setType("DEBIT");
        debit.setAmount(request.getAmount());
        debit.setBalanceAfter(senderBalance);
        debit.setCreatedAt(Instant.now());


        // Credit receiver
        long receiverBalance = ledgerRepository
                .findTopByUserIdOrderByCreatedAtDesc(receiver.getId())
                .map(LedgerEntryEntity::getBalanceAfter)
                .orElse(0L) + request.getAmount();
        receiver.setCurrentBalance(receiverBalance);

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

        userRepository.save(sender);
        userRepository.save(receiver);

//        // 2️⃣ Build request for Python Risk Engine (ML + Rules)
//        RiskEvaluationRequest riskRequest = new RiskEvaluationRequest(
//                request.getAmount(),
//                request.getLocation(),
//                request.getDevice().getDeviceId()
//        );

//        // 3️⃣ Call Python Risk Engine
//        RiskEvaluationResponse riskResponse =
//                riskEngineClientService.evaluateRisk(riskRequest);

//        // 4️⃣ Convert external response → internal RiskPacket
//        RiskPacket riskPacket = new RiskPacket();
//        riskPacket.setMlScore(riskResponse.getMlScore());
//        riskPacket.setRuleScore(riskResponse.getRuleScore());
//        riskPacket.setAnomalyScore(riskResponse.getAnomalyScore());
//        riskPacket.setReasons(riskResponse.getReasons());
//
//        // (Optional) log for debugging / demo
//        System.out.println("Risk Packet: " + riskPacket);
//
//        // 5️⃣ Send RiskPacket to Decision Agent
//        DecisionResponse decision =
//                decisionAgentClientService.evaluate(riskPacket);
//
//        // (Optional) log decision
//        System.out.println("Decision Taken: " + decision.getAction());
//
//        double finalRiskScore =
//                (riskPacket.getMlScore() * 0.5) +
//                        (riskPacket.getRuleScore() * 0.3) +
//                        (riskPacket.getAnomalyScore() * 0.2);


        // 6️⃣ Return final response to transaction app
        return new TransactionResponse(
                txnId,
                "SUCCESS",
                20.00,
                        "Transaction was Successfully carried out."
//                decision.getAction(),
//                finalRiskScore,
//                decision.getExplanation()
        );
    }
}
