package com.tksolutions.astraguard.service;

import com.tksolutions.astraguard.dto.RiskEvaluationRequest;
import com.tksolutions.astraguard.dto.RiskEvaluationResponse;
import com.tksolutions.astraguard.dto.ml.MlRiskRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class RiskEngineClientService {

    private final WebClient webClient;
    private final String mlBaseUrl;
    private final String evaluatePath;

    public RiskEngineClientService(
            WebClient webClient,
            @Value("${ml.base-url}") String mlBaseUrl,
            @Value("${ml.evaluate-path}") String evaluatePath
    ) {
        this.webClient = webClient;
        this.mlBaseUrl = mlBaseUrl;
        this.evaluatePath = evaluatePath;
    }

    public RiskEvaluationResponse evaluateRisk(MlRiskRequest request) {

        try {
            return webClient.post()
                    .uri(mlBaseUrl + evaluatePath)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RiskEvaluationResponse.class)
                    .block();

        } catch (WebClientResponseException ex) {
            // ML service responded but with error
            throw new RuntimeException(
                    "ML service error: " + ex.getResponseBodyAsString(), ex
            );

        } catch (Exception ex) {
            // ML service unreachable or timeout
            throw new RuntimeException(
                    "ML service unavailable", ex
            );
        }
    }
}
