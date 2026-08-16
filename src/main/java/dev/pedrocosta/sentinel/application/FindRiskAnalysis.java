package dev.pedrocosta.sentinel.application;

import dev.pedrocosta.sentinel.domain.model.RiskAssessment;

import java.util.UUID;

public interface FindRiskAnalysis {

    RiskAssessment findById(UUID analysisId);
}
