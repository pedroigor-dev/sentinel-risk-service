package dev.pedrocosta.sentinel.domain.model;

public record RiskFactor(String code, int points, String explanation) {

    public RiskFactor {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        if (points <= 0) {
            throw new IllegalArgumentException("points must be positive");
        }
        if (explanation == null || explanation.isBlank()) {
            throw new IllegalArgumentException("explanation is required");
        }
    }
}
