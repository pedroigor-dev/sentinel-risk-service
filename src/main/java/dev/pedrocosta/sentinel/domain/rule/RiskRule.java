package dev.pedrocosta.sentinel.domain.rule;

import dev.pedrocosta.sentinel.domain.model.RiskContext;
import dev.pedrocosta.sentinel.domain.model.RiskFactor;

import java.util.Optional;

@FunctionalInterface
public interface RiskRule {

    Optional<RiskFactor> evaluate(RiskContext context);
}
