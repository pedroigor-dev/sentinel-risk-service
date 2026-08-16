package dev.pedrocosta.sentinel.application;

import dev.pedrocosta.sentinel.application.event.RiskDecisionEvent;
import dev.pedrocosta.sentinel.application.exception.IdempotencyConflictException;
import dev.pedrocosta.sentinel.application.port.OutboxPort;
import dev.pedrocosta.sentinel.application.port.RiskAnalysisPort;
import dev.pedrocosta.sentinel.application.port.StoredAnalysis;
import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import dev.pedrocosta.sentinel.domain.model.RiskAssessment;
import dev.pedrocosta.sentinel.domain.rule.AmountRiskRule;
import dev.pedrocosta.sentinel.domain.service.RiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeTransactionServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-16T15:00:00Z");

    @Mock
    private RiskAnalysisPort analysisPort;

    @Mock
    private OutboxPort outboxPort;

    private RequestFingerprint fingerprint;
    private AnalyzeTransactionService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        fingerprint = new RequestFingerprint();
        service = new AnalyzeTransactionService(
                new RiskEngine(List.of(new AmountRiskRule()), clock),
                analysisPort,
                outboxPort,
                fingerprint,
                clock
        );
    }

    @Test
    void persistsTheAnalysisAndItsEventInOneUseCase() {
        AnalysisCommand command = command(NOW.minusSeconds(30));
        when(analysisPort.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(analysisPort.countCustomerTransactionsSince(eq("customer-7"), any()))
                .thenReturn(0L);

        RiskAssessment assessment = service.analyze(command, " idem-1 ");

        assertThat(assessment.score()).isEqualTo(20);
        verify(analysisPort).save(
                eq(assessment), eq(command), eq("idem-1"), eq(fingerprint.from(command))
        );
        ArgumentCaptor<RiskDecisionEvent> event = ArgumentCaptor.forClass(RiskDecisionEvent.class);
        verify(outboxPort).append(event.capture());
        assertThat(event.getValue().analysisId()).isEqualTo(assessment.analysisId());
    }

    @Test
    void returnsTheStoredDecisionWhenTheRequestIsAReplay() {
        AnalysisCommand command = command(NOW.minusSeconds(30));
        RiskAssessment stored = new RiskEngine(List.of(), Clock.fixed(NOW, ZoneOffset.UTC))
                .assess(new dev.pedrocosta.sentinel.domain.model.RiskContext(command, 0));
        when(analysisPort.findByIdempotencyKey("idem-2"))
                .thenReturn(Optional.of(new StoredAnalysis(stored, fingerprint.from(command))));

        RiskAssessment replay = service.analyze(command, "idem-2");

        assertThat(replay).isEqualTo(stored);
        verify(analysisPort, never()).save(any(), any(), any(), any());
        verify(outboxPort, never()).append(any());
    }

    @Test
    void rejectsAKeyReusedWithAnotherRequest() {
        AnalysisCommand command = command(NOW.minusSeconds(30));
        RiskAssessment stored = new RiskEngine(List.of(), Clock.fixed(NOW, ZoneOffset.UTC))
                .assess(new dev.pedrocosta.sentinel.domain.model.RiskContext(command, 0));
        when(analysisPort.findByIdempotencyKey("idem-3"))
                .thenReturn(Optional.of(new StoredAnalysis(stored, "another-fingerprint")));

        assertThatThrownBy(() -> service.analyze(command, "idem-3"))
                .isInstanceOf(IdempotencyConflictException.class);
    }

    @Test
    void rejectsTransactionsTooFarInTheFuture() {
        AnalysisCommand command = command(NOW.plusSeconds(301));
        when(analysisPort.findByIdempotencyKey("future")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.analyze(command, "future"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("occurredAt is too far in the future");
    }

    @Test
    void requiresABoundedIdempotencyKey() {
        AnalysisCommand command = command(NOW);

        assertThatThrownBy(() -> service.analyze(command, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Idempotency-Key is required");
        assertThatThrownBy(() -> service.analyze(command, "x".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Idempotency-Key cannot exceed 100 characters");
    }

    @Test
    void createsStableFingerprintsForEquivalentCommands() {
        AnalysisCommand first = command(NOW);
        AnalysisCommand second = command(NOW);

        assertThat(fingerprint.from(first))
                .hasSize(64)
                .isEqualTo(fingerprint.from(second));
    }

    private AnalysisCommand command(Instant occurredAt) {
        return new AnalysisCommand(
                "tx-42",
                "customer-7",
                new BigDecimal("5000.00"),
                "BRL",
                "BR",
                "BR",
                "5411",
                occurredAt
        );
    }
}
