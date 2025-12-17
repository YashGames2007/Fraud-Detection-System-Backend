package com.tksolutions.astraguard.dto;

import lombok.Data;

@Data
public class TransactionTransferRequest {

    private String toUpi;
    private Long amount;
    private String pin;
    private String transactionType;
    private Device device;
    private Location location;


    @Data
    public static class Device {
        private String deviceId;
        private String deviceType;
    }

    @Data
    public static class Location {
        private String city;
        private String country;
    }
}
