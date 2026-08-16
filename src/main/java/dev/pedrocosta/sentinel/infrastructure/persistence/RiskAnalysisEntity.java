package dev.pedrocosta.sentinel.infrastructure.persistence;

import dev.pedrocosta.sentinel.application.port.StoredAnalysis;
import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import dev.pedrocosta.sentinel.domain.model.RiskDecision;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "risk_analyses")
public class RiskAnalysisEntity {

    @Id
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "origin_country", nullable = false, length = 2)
    private String originCountry;

    @Column(name = "card_country", nullable = false, length = 2)
    private String cardCountry;

    @Column(name = "merchant_category", nullable = false, length = 4)
    private String merchantCategory;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "score", nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private RiskDecision decision;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "risk_analysis_factors",
            joinColumns = @JoinColumn(name = "analysis_id")
    )
    @OrderColumn(name = "factor_order")
    private List<RiskFactorEmbeddable> factors = new ArrayList<>();

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    protected RiskAnalysisEntity() {
    }

    RiskAnalysisEntity(
            RiskAssessment assessment,
            AnalysisCommand command,
            String idempotencyKey,
            String requestFingerprint
    ) {
        this.id = assessment.analysisId();
        this.idempotencyKey = idempotencyKey;
        this.requestFingerprint = requestFingerprint;
        this.transactionId = command.transactionId();
        this.customerId = command.customerId();
        this.amount = command.amount();
        this.currency = command.currency();
        this.originCountry = command.originCountry();
        this.cardCountry = command.cardCountry();
        this.merchantCategory = command.merchantCategory();
        this.occurredAt = command.occurredAt();
        this.score = assessment.score();
        this.decision = assessment.decision();
        this.factors = assessment.factors().stream().map(RiskFactorEmbeddable::from).toList();
        this.analyzedAt = assessment.analyzedAt();
    }

    StoredAnalysis toStoredAnalysis() {
        RiskAssessment assessment = new RiskAssessment(
                id,
                transactionId,
                customerId,
                score,
                decision,
                factors.stream().map(RiskFactorEmbeddable::toDomain).toList(),
                analyzedAt
        );
        return new StoredAnalysis(assessment, requestFingerprint);
    }
}
