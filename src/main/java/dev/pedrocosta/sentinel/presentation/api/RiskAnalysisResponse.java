package dev.pedrocosta.sentinel.presentation.api;

import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import dev.pedrocosta.sentinel.domain.model.RiskDecision;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RiskAnalysisResponse(
        UUID analysisId,
        String transactionId,
        String customerId,
        int score,
        RiskDecision decision,
        List<RiskFactorResponse> factors,
        Instant analyzedAt
) {

    public RiskAnalysisResponse {
        factors = List.copyOf(factors);
    }

    static RiskAnalysisResponse from(RiskAssessment assessment) {
        List<RiskFactorResponse> factors = assessment.factors().stream()
                .map(factor -> new RiskFactorResponse(
                        factor.code(), factor.points(), factor.explanation()
                ))
                .toList();
        return new RiskAnalysisResponse(
                assessment.analysisId(),
                assessment.transactionId(),
                assessment.customerId(),
                assessment.score(),
                assessment.decision(),
                factors,
                assessment.analyzedAt()
        );
    }

    public record RiskFactorResponse(String code, int points, String explanation) {
    }
}
