package dev.pedrocosta.sentinel.domain.rule;

import dev.pedrocosta.sentinel.domain.model.RiskContext;
import dev.pedrocosta.sentinel.domain.model.RiskFactor;

import java.util.Optional;
import java.util.Set;

public final class MerchantCategoryRiskRule implements RiskRule {

    private static final Set<String> HIGH_RISK_CATEGORIES = Set.of("6051", "7995");

    @Override
    public Optional<RiskFactor> evaluate(RiskContext context) {
        if (HIGH_RISK_CATEGORIES.contains(context.command().merchantCategory())) {
            return Optional.of(new RiskFactor(
                    "HIGH_RISK_MERCHANT", 30, "Merchant category has a higher fraud exposure"
            ));
        }
        return Optional.empty();
    }
}
