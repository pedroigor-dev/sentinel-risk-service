package dev.pedrocosta.sentinel.domain.service;

import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import dev.pedrocosta.sentinel.domain.model.RiskContext;
import dev.pedrocosta.sentinel.domain.model.RiskDecision;
import dev.pedrocosta.sentinel.domain.rule.AmountRiskRule;
import dev.pedrocosta.sentinel.domain.rule.CountryMismatchRiskRule;
import dev.pedrocosta.sentinel.domain.rule.MerchantCategoryRiskRule;
import dev.pedrocosta.sentinel.domain.rule.NightTransactionRiskRule;
import dev.pedrocosta.sentinel.domain.rule.RiskRule;
import dev.pedrocosta.sentinel.domain.rule.VelocityRiskRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskEngineTest {

    private static final Instant NOW = Instant.parse("2026-08-16T15:00:00Z");

    private RiskEngine engine;

    @BeforeEach
    void setUp() {
        List<RiskRule> rules = List.of(
                new AmountRiskRule(),
                new CountryMismatchRiskRule(),
                new MerchantCategoryRiskRule(),
                new NightTransactionRiskRule(),
                new VelocityRiskRule()
        );
        engine = new RiskEngine(rules, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void approvesAnOrdinaryTransaction() {
        RiskAssessment assessment = engine.assess(context(
                "120.00", "BR", "BR", "5411", "2026-08-16T12:00:00Z", 0
        ));

        assertThat(assessment.decision()).isEqualTo(RiskDecision.APPROVED);
        assertThat(assessment.score()).isZero();
        assertThat(assessment.factors()).isEmpty();
        assertThat(assessment.analyzedAt()).isEqualTo(NOW);
    }

    @Test
    void sendsAnAmbiguousTransactionToReview() {
        RiskAssessment assessment = engine.assess(context(
                "5000.00", "BR", "US", "5411", "2026-08-16T12:00:00Z", 0
        ));

        assertThat(assessment.decision()).isEqualTo(RiskDecision.REVIEW);
        assertThat(assessment.score()).isEqualTo(45);
        assertThat(assessment.factors()).extracting("code")
                .containsExactly("COUNTRY_MISMATCH", "ELEVATED_AMOUNT");
    }

    @Test
    void declinesACombinationOfStrongSignals() {
        RiskAssessment assessment = engine.assess(context(
                "12000.00", "BR", "US", "7995", "2026-08-16T03:00:00Z", 4
        ));

        assertThat(assessment.decision()).isEqualTo(RiskDecision.DECLINED);
        assertThat(assessment.score()).isEqualTo(100);
        assertThat(assessment.factors()).hasSize(5);
    }

    @Test
    void capsTheRiskScoreAtOneHundred() {
        RiskAssessment assessment = engine.assess(context(
                "12000.00", "BR", "US", "6051", "2026-08-16T01:00:00Z", 10
        ));

        assertThat(assessment.score()).isEqualTo(100);
    }

    private RiskContext context(
            String amount,
            String originCountry,
            String cardCountry,
            String merchantCategory,
            String occurredAt,
            long recentTransactionCount
    ) {
        AnalysisCommand command = new AnalysisCommand(
                "tx-42",
                "customer-7",
                new BigDecimal(amount),
                "BRL",
                originCountry,
                cardCountry,
                merchantCategory,
                Instant.parse(occurredAt)
        );
        return new RiskContext(command, recentTransactionCount);
    }
}
