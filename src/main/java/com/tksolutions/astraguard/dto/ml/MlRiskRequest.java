package com.tksolutions.astraguard.dto.ml;

import lombok.Data;

@Data
public class MlRiskRequest {

    // Transaction sequence
    private long step;
    private long prevStep;

    // Transaction type (tokenized)
    private String typeToken;

    // Amount (NOT tokenized)
    private double amount;

    // Sender
    private String nameOrigToken;
    private double oldBalanceOrg;
    private double newBalanceOrig;

    // Receiver
    private String nameDestToken;
    private double oldBalanceDest;
    private double newBalanceDest;
}
