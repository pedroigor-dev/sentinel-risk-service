package dev.pedrocosta.sentinel.infrastructure.persistence;

import dev.pedrocosta.sentinel.application.port.RiskAnalysisPort;
import dev.pedrocosta.sentinel.application.port.StoredAnalysis;
import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class JpaRiskAnalysisAdapter implements RiskAnalysisPort {

    private final RiskAnalysisJpaRepository repository;

    public JpaRiskAnalysisAdapter(RiskAnalysisJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<StoredAnalysis> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .map(RiskAnalysisEntity::toStoredAnalysis);
    }

    @Override
    public long countCustomerTransactionsSince(String customerId, Instant windowStart) {
        return repository.countByCustomerIdAndOccurredAtGreaterThanEqual(customerId, windowStart);
    }

    @Override
    public void save(
            RiskAssessment assessment,
            AnalysisCommand command,
            String idempotencyKey,
            String requestFingerprint
    ) {
        repository.save(new RiskAnalysisEntity(
                assessment, command, idempotencyKey, requestFingerprint
        ));
    }
}
