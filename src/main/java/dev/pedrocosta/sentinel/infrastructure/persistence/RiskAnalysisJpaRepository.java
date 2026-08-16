package dev.pedrocosta.sentinel.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

interface RiskAnalysisJpaRepository extends JpaRepository<RiskAnalysisEntity, UUID> {

    Optional<RiskAnalysisEntity> findByIdempotencyKey(String idempotencyKey);

    long countByCustomerIdAndOccurredAtGreaterThanEqual(String customerId, Instant windowStart);
}
