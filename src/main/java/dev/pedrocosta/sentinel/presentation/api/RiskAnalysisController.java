package dev.pedrocosta.sentinel.presentation.api;

import dev.pedrocosta.sentinel.application.AnalyzeTransaction;
import dev.pedrocosta.sentinel.application.FindRiskAnalysis;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analyses")
public class RiskAnalysisController {

    private final AnalyzeTransaction analyzeTransaction;
    private final FindRiskAnalysis findRiskAnalysis;

    public RiskAnalysisController(
            AnalyzeTransaction analyzeTransaction,
            FindRiskAnalysis findRiskAnalysis
    ) {
        this.analyzeTransaction = analyzeTransaction;
        this.findRiskAnalysis = findRiskAnalysis;
    }

    @PostMapping
    ResponseEntity<RiskAnalysisResponse> analyze(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AnalyzeTransactionRequest request
    ) {
        RiskAssessment assessment = analyzeTransaction.analyze(
                request.toCommand(), idempotencyKey
        );
        URI location = URI.create("/api/v1/analyses/" + assessment.analysisId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(RiskAnalysisResponse.from(assessment));
    }

    @GetMapping("/{analysisId}")
    RiskAnalysisResponse findById(@PathVariable UUID analysisId) {
        return RiskAnalysisResponse.from(findRiskAnalysis.findById(analysisId));
    }
}
