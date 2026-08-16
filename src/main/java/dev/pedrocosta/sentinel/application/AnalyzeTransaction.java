package dev.pedrocosta.sentinel.application;

import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;

public interface AnalyzeTransaction {

    RiskAssessment analyze(AnalysisCommand command, String idempotencyKey);
}
