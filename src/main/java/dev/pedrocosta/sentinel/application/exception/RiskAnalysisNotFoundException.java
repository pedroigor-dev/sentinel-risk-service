package dev.pedrocosta.sentinel.application.exception;

import java.util.UUID;

public class RiskAnalysisNotFoundException extends RuntimeException {

    public RiskAnalysisNotFoundException(UUID analysisId) {
        super("Risk analysis " + analysisId + " was not found");
    }
}
