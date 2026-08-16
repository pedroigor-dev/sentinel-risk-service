package dev.pedrocosta.sentinel.domain.rule;

import dev.pedrocosta.sentinel.domain.model.RiskContext;
import dev.pedrocosta.sentinel.domain.model.RiskFactor;

import java.util.Optional;

public final class CountryMismatchRiskRule implements RiskRule {

    @Override
    public Optional<RiskFactor> evaluate(RiskContext context) {
        if (!context.command().originCountry().equals(context.command().cardCountry())) {
            return Optional.of(new RiskFactor(
                    "COUNTRY_MISMATCH", 25, "Transaction and card countries do not match"
            ));
        }
        return Optional.empty();
    }
}
