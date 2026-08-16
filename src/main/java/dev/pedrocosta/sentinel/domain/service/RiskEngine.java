package dev.pedrocosta.sentinel.domain.service;

import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import dev.pedrocosta.sentinel.domain.model.RiskContext;
import dev.pedrocosta.sentinel.domain.model.RiskDecision;
import dev.pedrocosta.sentinel.domain.model.RiskFactor;
import dev.pedrocosta.sentinel.domain.rule.RiskRule;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class RiskEngine {

    private static final int DECLINE_THRESHOLD = 70;
    private static final int REVIEW_THRESHOLD = 40;

    private final List<RiskRule> rules;
    private final Clock clock;

    public RiskEngine(List<RiskRule> rules, Clock clock) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules are required"));
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    public RiskAssessment assess(RiskContext context) {
        List<RiskFactor> factors = rules.stream()
                .map(rule -> rule.evaluate(context))
                .flatMap(java.util.Optional::stream)
                .sorted(Comparator.comparingInt(RiskFactor::points).reversed())
                .toList();

        int score = Math.min(100, factors.stream().mapToInt(RiskFactor::points).sum());
        return new RiskAssessment(
                UUID.randomUUID(),
                context.command().transactionId(),
                context.command().customerId(),
                score,
                decisionFor(score),
                factors,
                Instant.now(clock)
        );
    }

    private RiskDecision decisionFor(int score) {
        if (score >= DECLINE_THRESHOLD) {
            return RiskDecision.DECLINED;
        }
        if (score >= REVIEW_THRESHOLD) {
            return RiskDecision.REVIEW;
        }
        return RiskDecision.APPROVED;
    }
}
