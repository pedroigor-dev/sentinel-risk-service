package dev.pedrocosta.sentinel.domain.rule;

import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import dev.pedrocosta.sentinel.domain.model.RiskContext;
import dev.pedrocosta.sentinel.domain.model.RiskFactor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RiskRulesTest {

    @Test
    void assignsMorePointsToVeryHighAmounts() {
        Optional<RiskFactor> factor = new AmountRiskRule().evaluate(context(
                "12000.00", "BR", "BR", "5411", "2026-08-16T12:00:00Z", 0
        ));

        assertThat(factor).contains(new RiskFactor(
                "HIGH_AMOUNT", 35, "Transaction amount is at least 10,000.00"
        ));
    }

    @Test
    void ignoresOrdinaryAmounts() {
        Optional<RiskFactor> factor = new AmountRiskRule().evaluate(context(
                "350.00", "BR", "BR", "5411", "2026-08-16T12:00:00Z", 0
        ));

        assertThat(factor).isEmpty();
    }

    @Test
    void detectsCountryMismatch() {
        Optional<RiskFactor> factor = new CountryMismatchRiskRule().evaluate(context(
                "350.00", "BR", "US", "5411", "2026-08-16T12:00:00Z", 0
        ));

        assertThat(factor).map(RiskFactor::code).contains("COUNTRY_MISMATCH");
    }

    @Test
    void detectsHighRiskMerchantCategory() {
        Optional<RiskFactor> factor = new MerchantCategoryRiskRule().evaluate(context(
                "350.00", "BR", "BR", "7995", "2026-08-16T12:00:00Z", 0
        ));

        assertThat(factor).map(RiskFactor::points).contains(30);
    }

    @Test
    void detectsUnusualUtcHour() {
        Optional<RiskFactor> factor = new NightTransactionRiskRule().evaluate(context(
                "350.00", "BR", "BR", "5411", "2026-08-16T03:00:00Z", 0
        ));

        assertThat(factor).map(RiskFactor::code).contains("UNUSUAL_HOUR");
    }

    @Test
    void detectsCustomerVelocity() {
        Optional<RiskFactor> factor = new VelocityRiskRule().evaluate(context(
                "350.00", "BR", "BR", "5411", "2026-08-16T12:00:00Z", 3
        ));

        assertThat(factor).map(RiskFactor::code).contains("HIGH_VELOCITY");
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
