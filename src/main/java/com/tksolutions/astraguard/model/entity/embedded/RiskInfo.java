package com.tksolutions.astraguard.model.entity.embedded;

import java.util.List;

public class RiskInfo {
    private double riskScore;
    private String decision; // ALLOW / BLOCK
    private List<String> reasons;
    // getters & setters


    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}

