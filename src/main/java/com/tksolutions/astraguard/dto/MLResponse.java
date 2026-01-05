//package com.tksolutions.astraguard.dto;
//
//import lombok.Data;
//
//@Data
//public class MLResponse {
//    private Double fraud_probability;
//    private Double anomaly_score;
//    private String model_reason;
//}
package com.tksolutions.astraguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class MLResponse {
    // We use @JsonProperty to map Python's "risk_score" to Java's "riskScore"
    @JsonProperty("risk_score")
    private Double riskScore;

    @JsonProperty("action")
    private String action;

    // In your Python code, this was likely "reasons" or not yet implemented
    @JsonProperty("reasons")
    private List<String> reasons;
}