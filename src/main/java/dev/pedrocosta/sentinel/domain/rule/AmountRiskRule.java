package dev.pedrocosta.sentinel.domain.rule;

import dev.pedrocosta.sentinel.domain.model.RiskContext;
import dev.pedrocosta.sentinel.domain.model.RiskFactor;

import java.math.BigDecimal;
import java.util.Optional;

public final class AmountRiskRule implements RiskRule {

    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("10000.00");
    private static final BigDecimal ELEVATED_AMOUNT = new BigDecimal("5000.00");

    @Override
    public Optional<RiskFactor> evaluate(RiskContext context) {
        BigDecimal amount = context.command().amount();
        if (amount.compareTo(HIGH_AMOUNT) >= 0) {
            return Optional.of(new RiskFactor(
                    "HIGH_AMOUNT", 35, "Transaction amount is at least 10,000.00"
            ));
        }
        if (amount.compareTo(ELEVATED_AMOUNT) >= 0) {
            return Optional.of(new RiskFactor(
                    "ELEVATED_AMOUNT", 20, "Transaction amount is at least 5,000.00"
            ));
        }
        return Optional.empty();
    }
}
