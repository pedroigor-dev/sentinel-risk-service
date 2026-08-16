package dev.pedrocosta.sentinel.domain.model;

import java.util.Objects;

public record RiskContext(AnalysisCommand command, long recentTransactionCount) {

    public RiskContext {
        Objects.requireNonNull(command, "command is required");
        if (recentTransactionCount < 0) {
            throw new IllegalArgumentException("recentTransactionCount cannot be negative");
        }
    }
}
