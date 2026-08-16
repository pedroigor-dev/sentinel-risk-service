package dev.pedrocosta.sentinel.application.port;

import dev.pedrocosta.sentinel.domain.model.RiskAssessment;

import java.util.Objects;

public record StoredAnalysis(RiskAssessment assessment, String requestFingerprint) {

    public StoredAnalysis {
        Objects.requireNonNull(assessment, "assessment is required");
        if (requestFingerprint == null || requestFingerprint.isBlank()) {
            throw new IllegalArgumentException("requestFingerprint is required");
        }
    }
}
