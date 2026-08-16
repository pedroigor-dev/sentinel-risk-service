package dev.pedrocosta.sentinel.configuration;

import dev.pedrocosta.sentinel.domain.rule.AmountRiskRule;
import dev.pedrocosta.sentinel.domain.rule.CountryMismatchRiskRule;
import dev.pedrocosta.sentinel.domain.rule.MerchantCategoryRiskRule;
import dev.pedrocosta.sentinel.domain.rule.NightTransactionRiskRule;
import dev.pedrocosta.sentinel.domain.rule.RiskRule;
import dev.pedrocosta.sentinel.domain.rule.VelocityRiskRule;
import dev.pedrocosta.sentinel.domain.service.RiskEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
public class DomainConfiguration {

    @Bean
    Clock utcClock() {
        return Clock.systemUTC();
    }

    @Bean
    RiskEngine riskEngine(Clock clock) {
        List<RiskRule> rules = List.of(
                new AmountRiskRule(),
                new CountryMismatchRiskRule(),
                new MerchantCategoryRiskRule(),
                new NightTransactionRiskRule(),
                new VelocityRiskRule()
        );
        return new RiskEngine(rules, clock);
    }
}
