package dev.pedrocosta.sentinel.application;

import dev.pedrocosta.sentinel.application.exception.RiskAnalysisNotFoundException;
import dev.pedrocosta.sentinel.application.port.RiskAnalysisPort;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FindRiskAnalysisService implements FindRiskAnalysis {

    private final RiskAnalysisPort analysisPort;

    public FindRiskAnalysisService(RiskAnalysisPort analysisPort) {
        this.analysisPort = analysisPort;
    }

    @Override
    @Transactional(readOnly = true)
    public RiskAssessment findById(UUID analysisId) {
        return analysisPort.findById(analysisId)
                .orElseThrow(() -> new RiskAnalysisNotFoundException(analysisId));
    }
}
