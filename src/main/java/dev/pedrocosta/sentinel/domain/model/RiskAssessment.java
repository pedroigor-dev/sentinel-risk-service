package dev.pedrocosta.sentinel.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record RiskAssessment(
        UUID analysisId,
        String transactionId,
        String customerId,
        int score,
        RiskDecision decision,
        List<RiskFactor> factors,
        Instant analyzedAt
) {

    public RiskAssessment {
        Objects.requireNonNull(analysisId, "analysisId is required");
        Objects.requireNonNull(decision, "decision is required");
        Objects.requireNonNull(analyzedAt, "analyzedAt is required");
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId is required");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
        factors = List.copyOf(Objects.requireNonNull(factors, "factors are required"));
    }
}
