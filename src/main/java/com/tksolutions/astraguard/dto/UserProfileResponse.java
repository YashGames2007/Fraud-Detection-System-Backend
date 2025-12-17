package com.tksolutions.astraguard.dto;

public class UserProfileResponse {

    private String upiId;
    private String name;
    private String mobile;

    public UserProfileResponse(String upiId, String name, String mobile) {
        this.upiId = upiId;
        this.name = name;
        this.mobile = mobile;
    }

    public String getUpiId() {
        return upiId;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }
}
