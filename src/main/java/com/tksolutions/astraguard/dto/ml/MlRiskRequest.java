//package com.tksolutions.astraguard.dto.ml;
//
//import lombok.Data;
//
//@Data
//public class MlRiskRequest {
//
//    // Transaction sequence
//    private long step;
//    private long prevStep;
//
//    // Transaction type (tokenized)
//    private String typeToken;
//
//    // Amount (NOT tokenized)
//    private double amount;
//
//    // Sender
//    private String nameOrigToken;
//    private double oldBalanceOrg;
//    private double newBalanceOrig;
//
//    // Receiver
//    private String nameDestToken;
//    private double oldBalanceDest;
//    private double newBalanceDest;
//}
package com.tksolutions.astraguard.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MlRiskRequest {

    private long step;

    @JsonProperty("prev_step")
    private long prevStep;

    @JsonProperty("type_token")
    private String typeToken;

    private double amount;

    @JsonProperty("nameOrig_token")
    private String nameOrigToken;

    @JsonProperty("oldbalanceOrg")
    private double oldBalanceOrg;

    @JsonProperty("newbalanceOrig")
    private double newBalanceOrig;

    @JsonProperty("nameDest_token")
    private String nameDestToken;

    @JsonProperty("oldbalanceDest")
    private double oldBalanceDest;

    @JsonProperty("newbalanceDest")
    private double newBalanceDest;
}