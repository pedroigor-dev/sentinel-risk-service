package dev.pedrocosta.sentinel.application;

import dev.pedrocosta.sentinel.application.exception.RiskAnalysisNotFoundException;
import dev.pedrocosta.sentinel.application.port.RiskAnalysisPort;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import dev.pedrocosta.sentinel.domain.model.RiskDecision;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FindRiskAnalysisServiceTest {

    @Test
    void returnsAnExistingAnalysis() {
        RiskAnalysisPort port = mock(RiskAnalysisPort.class);
        UUID id = UUID.randomUUID();
        RiskAssessment assessment = assessment(id);
        when(port.findById(id)).thenReturn(Optional.of(assessment));

        assertThat(new FindRiskAnalysisService(port).findById(id)).isEqualTo(assessment);
    }

    @Test
    void reportsAnUnknownAnalysis() {
        RiskAnalysisPort port = mock(RiskAnalysisPort.class);
        UUID id = UUID.randomUUID();
        when(port.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new FindRiskAnalysisService(port).findById(id))
                .isInstanceOf(RiskAnalysisNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    private RiskAssessment assessment(UUID id) {
        return new RiskAssessment(
                id,
                "tx-1",
                "customer-1",
                0,
                RiskDecision.APPROVED,
                List.of(),
                Instant.parse("2026-08-16T12:00:00Z")
        );
    }
}
