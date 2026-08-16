package dev.pedrocosta.sentinel.infrastructure.persistence;

import dev.pedrocosta.sentinel.domain.model.RiskFactor;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RiskFactorEmbeddable {

    @Column(name = "factor_code", nullable = false, length = 50)
    private String code;

    @Column(name = "points", nullable = false)
    private int points;

    @Column(name = "explanation", nullable = false, length = 255)
    private String explanation;

    protected RiskFactorEmbeddable() {
    }

    private RiskFactorEmbeddable(RiskFactor factor) {
        this.code = factor.code();
        this.points = factor.points();
        this.explanation = factor.explanation();
    }

    static RiskFactorEmbeddable from(RiskFactor factor) {
        return new RiskFactorEmbeddable(factor);
    }

    RiskFactor toDomain() {
        return new RiskFactor(code, points, explanation);
    }
}
