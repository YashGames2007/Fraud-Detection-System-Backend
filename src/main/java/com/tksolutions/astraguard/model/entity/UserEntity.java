package com.tksolutions.astraguard.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document(collection = "users")
public class UserEntity {

    @Id
    private String id;

    @Field("upi_id")
    private String upiId;

    private String name;
    private String mobile;

    @Field("current_balance")
    private Long currentBalance;

    @Field("device_ids")
    private List<String> deviceIds;

    @Field("created_at")
    private Instant createdAt;
    @Field("updated_at")
    private Instant updatedAt;


    @Field("password_hash")
    private String passwordHash;
    @Field("pin_hash")
    private String pinHash;

    @Field("failed_pin_attempts")
    private int failedPinAttempts;
    @Field("pin_locked_until")
    private Instant pinLockedUntil;


    // Constructors
    public UserEntity() {}

    public UserEntity(String id, String upiId, String name,
                      Long currentBalance, List<String> deviceIds,
                      Instant createdAt) { this.id = id;
        this.upiId = upiId;
        this.name = name;
        this.currentBalance = currentBalance;
        this.deviceIds = deviceIds;
        this.createdAt = createdAt; }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(Long currentBalance) { this.currentBalance = currentBalance; }

    public List<String> getDeviceIds() { return deviceIds; }
    public void setDeviceIds(List<String> deviceIds) { this.deviceIds = deviceIds; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public int getFailedPinAttempts() { return failedPinAttempts; }
    public void setFailedPinAttempts(int failedPinAttempts) { this.failedPinAttempts = failedPinAttempts; }

    public Instant getPinLockedUntil() { return pinLockedUntil; }
    public void setPinLockedUntil(Instant pinLockedUntil) { this.pinLockedUntil = pinLockedUntil; }
}
