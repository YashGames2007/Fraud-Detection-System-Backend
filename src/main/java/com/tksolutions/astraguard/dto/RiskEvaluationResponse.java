//package com.tksolutions.astraguard.dto;
//
//import lombok.Data;
//import java.util.List;
//
//@Data
//public class RiskEvaluationResponse {
//
//    private Double mlScore;
//    private Double ruleScore;
//    private Double anomalyScore;
//    private List<String> reasons;
//}
package com.tksolutions.astraguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class RiskEvaluationResponse {

    // 1. Map the Python 'risk_score' to this field
    @JsonProperty("risk_score")
    private Double riskScore;

    // 2. Map the Python 'action' to this field
    @JsonProperty("action")
    private String decision;

    // 3. This matches 'reasons' from the JSON
    private List<String> reasons;

    // Optional: Keep this if your code specifically calls .getMlScore()
    // but redirect it to riskScore to avoid breaking old code
    public Double getMlScore() {
        return this.riskScore;
    }
}