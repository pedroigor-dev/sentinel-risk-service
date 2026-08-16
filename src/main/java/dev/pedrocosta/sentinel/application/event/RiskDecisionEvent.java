package dev.pedrocosta.sentinel.application.event;

import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import dev.pedrocosta.sentinel.domain.model.RiskDecision;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RiskDecisionEvent(
        UUID eventId,
        UUID analysisId,
        String transactionId,
        String customerId,
        BigDecimal amount,
        String currency,
        int score,
        RiskDecision decision,
        List<String> factorCodes,
        Instant occurredAt,
        Instant analyzedAt
) {

    public RiskDecisionEvent {
        factorCodes = List.copyOf(factorCodes);
    }

    public static RiskDecisionEvent from(RiskAssessment assessment, AnalysisCommand command) {
        List<String> factorCodes = assessment.factors().stream()
                .map(factor -> factor.code())
                .toList();
        return new RiskDecisionEvent(
                UUID.randomUUID(),
                assessment.analysisId(),
                assessment.transactionId(),
                assessment.customerId(),
                command.amount(),
                command.currency(),
                assessment.score(),
                assessment.decision(),
                factorCodes,
                command.occurredAt(),
                assessment.analyzedAt()
        );
    }
}
