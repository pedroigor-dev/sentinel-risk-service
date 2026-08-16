package dev.pedrocosta.sentinel.presentation.api;

import dev.pedrocosta.sentinel.domain.model.RiskDecision;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskAnalysisResponseTest {

    @Test
    void keepsAnImmutableSnapshotOfFactors() {
        RiskAnalysisResponse.RiskFactorResponse highVelocity =
                new RiskAnalysisResponse.RiskFactorResponse("HIGH_VELOCITY", 30, "Three recent transactions");
        List<RiskAnalysisResponse.RiskFactorResponse> factors = new ArrayList<>(List.of(highVelocity));
        RiskAnalysisResponse response = new RiskAnalysisResponse(
                UUID.randomUUID(), "txn-1", "customer-1", 30, RiskDecision.APPROVED,
                factors, Instant.parse("2026-08-16T12:00:01Z")
        );

        factors.clear();

        assertThat(response.factors()).containsExactly(highVelocity);
        assertThatThrownBy(() -> response.factors().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
