package dev.pedrocosta.sentinel.domain.rule;

import dev.pedrocosta.sentinel.domain.model.RiskContext;
import dev.pedrocosta.sentinel.domain.model.RiskFactor;

import java.util.Optional;

public final class VelocityRiskRule implements RiskRule {

    private static final long RECENT_TRANSACTION_LIMIT = 3;

    @Override
    public Optional<RiskFactor> evaluate(RiskContext context) {
        if (context.recentTransactionCount() >= RECENT_TRANSACTION_LIMIT) {
            return Optional.of(new RiskFactor(
                    "HIGH_VELOCITY", 30, "Customer already has three transactions in the last hour"
            ));
        }
        return Optional.empty();
    }
}
