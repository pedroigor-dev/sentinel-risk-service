package dev.pedrocosta.sentinel.application.port;

import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;

import java.time.Instant;
import java.util.Optional;

public interface RiskAnalysisPort {

    Optional<StoredAnalysis> findByIdempotencyKey(String idempotencyKey);

    long countCustomerTransactionsSince(String customerId, Instant windowStart);

    void save(
            RiskAssessment assessment,
            AnalysisCommand command,
            String idempotencyKey,
            String requestFingerprint
    );
}
