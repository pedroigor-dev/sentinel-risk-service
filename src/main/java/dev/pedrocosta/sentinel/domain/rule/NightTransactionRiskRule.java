package dev.pedrocosta.sentinel.domain.rule;

import dev.pedrocosta.sentinel.domain.model.RiskContext;
import dev.pedrocosta.sentinel.domain.model.RiskFactor;

import java.time.ZoneOffset;
import java.util.Optional;

public final class NightTransactionRiskRule implements RiskRule {

    @Override
    public Optional<RiskFactor> evaluate(RiskContext context) {
        int hour = context.command().occurredAt().atZone(ZoneOffset.UTC).getHour();
        if (hour < 6) {
            return Optional.of(new RiskFactor(
                    "UNUSUAL_HOUR", 15, "Transaction occurred between midnight and 06:00 UTC"
            ));
        }
        return Optional.empty();
    }
}
