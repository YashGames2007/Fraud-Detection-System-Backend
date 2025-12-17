package com.tksolutions.astraguard.model.entity;

import com.tksolutions.astraguard.model.entity.embedded.DeviceInfo;
import com.tksolutions.astraguard.model.entity.embedded.LocationInfo;
import com.tksolutions.astraguard.model.entity.embedded.NetworkInfo;
import com.tksolutions.astraguard.model.entity.embedded.RiskInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document(collection = "transactions")
public class TransactionEntity {

    @Id
    private String id;

    @Field("sender_id")
    private String senderId;
    @Field("receiver_id")
    private String receiverId;

    private long amount;

    private String status; // SUCCESS / BLOCKED / FAILED
    @Field("transaction_type")
    private String transactionType; // QR_CODE / BANK_TO_BANK / DIRECT_PHONE_NUMBER

    // === Fraud-related context ===
    private DeviceInfo device;
    private LocationInfo location;
    private NetworkInfo network;
    private RiskInfo risk;

    private Instant createdAt;
    private Instant updatedAt;

    // getters & setters


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public DeviceInfo getDevice() {
        return device;
    }

    public void setDevice(DeviceInfo device) {
        this.device = device;
    }

    public LocationInfo getLocation() {
        return location;
    }

    public void setLocation(LocationInfo location) {
        this.location = location;
    }

    public NetworkInfo getNetwork() {
        return network;
    }

    public void setNetwork(NetworkInfo network) {
        this.network = network;
    }

    public RiskInfo getRisk() {
        return risk;
    }

    public void setRisk(RiskInfo risk) {
        this.risk = risk;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
