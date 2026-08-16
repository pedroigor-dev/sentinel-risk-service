package dev.pedrocosta.sentinel.application;

import dev.pedrocosta.sentinel.application.event.RiskDecisionEvent;
import dev.pedrocosta.sentinel.application.exception.IdempotencyConflictException;
import dev.pedrocosta.sentinel.application.port.OutboxPort;
import dev.pedrocosta.sentinel.application.port.RiskAnalysisPort;
import dev.pedrocosta.sentinel.application.port.StoredAnalysis;
import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import dev.pedrocosta.sentinel.domain.model.RiskContext;
import dev.pedrocosta.sentinel.domain.service.RiskEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
public class AnalyzeTransactionService implements AnalyzeTransaction {

    private static final Duration VELOCITY_WINDOW = Duration.ofHours(1);
    private static final Duration MAX_CLOCK_SKEW = Duration.ofMinutes(5);

    private final RiskEngine riskEngine;
    private final RiskAnalysisPort analysisPort;
    private final OutboxPort outboxPort;
    private final RequestFingerprint fingerprint;
    private final Clock clock;

    public AnalyzeTransactionService(
            RiskEngine riskEngine,
            RiskAnalysisPort analysisPort,
            OutboxPort outboxPort,
            RequestFingerprint fingerprint,
            Clock clock
    ) {
        this.riskEngine = Objects.requireNonNull(riskEngine);
        this.analysisPort = Objects.requireNonNull(analysisPort);
        this.outboxPort = Objects.requireNonNull(outboxPort);
        this.fingerprint = Objects.requireNonNull(fingerprint);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    @Transactional
    public RiskAssessment analyze(AnalysisCommand command, String idempotencyKey) {
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        String requestFingerprint = fingerprint.from(command);
        Optional<StoredAnalysis> previous = analysisPort.findByIdempotencyKey(normalizedKey);

        if (previous.isPresent()) {
            return resolvePrevious(previous.orElseThrow(), requestFingerprint);
        }

        rejectTransactionsTooFarInTheFuture(command);
        Instant windowStart = command.occurredAt().minus(VELOCITY_WINDOW);
        long recentTransactions = analysisPort.countCustomerTransactionsSince(
                command.customerId(), windowStart
        );
        RiskAssessment assessment = riskEngine.assess(new RiskContext(command, recentTransactions));

        analysisPort.save(assessment, command, normalizedKey, requestFingerprint);
        outboxPort.append(RiskDecisionEvent.from(assessment, command));
        return assessment;
    }

    private RiskAssessment resolvePrevious(StoredAnalysis previous, String requestFingerprint) {
        if (!previous.requestFingerprint().equals(requestFingerprint)) {
            throw new IdempotencyConflictException(
                    "The idempotency key was already used with a different request"
            );
        }
        return previous.assessment();
    }

    private void rejectTransactionsTooFarInTheFuture(AnalysisCommand command) {
        Instant acceptedLimit = Instant.now(clock).plus(MAX_CLOCK_SKEW);
        if (command.occurredAt().isAfter(acceptedLimit)) {
            throw new IllegalArgumentException("occurredAt is too far in the future");
        }
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key is required");
        }
        String normalized = idempotencyKey.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Idempotency-Key cannot exceed 100 characters");
        }
        return normalized;
    }
}
