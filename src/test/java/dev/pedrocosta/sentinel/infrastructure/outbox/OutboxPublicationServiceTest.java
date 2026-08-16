package dev.pedrocosta.sentinel.infrastructure.outbox;

import dev.pedrocosta.sentinel.configuration.OutboxProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPublicationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-16T15:00:00Z");

    @Mock
    private OutboxEventJpaRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private SimpleMeterRegistry meterRegistry;
    private OutboxPublicationService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new OutboxPublicationService(
                repository,
                kafkaTemplate,
                new OutboxProperties("risk.decisions.test", Duration.ofSeconds(1)),
                Clock.fixed(NOW, ZoneOffset.UTC),
                meterRegistry
        );
    }

    @Test
    void marksAnAcknowledgedEventAsPublished() {
        OutboxEventEntity event = event();
        when(repository.findById(event.id())).thenReturn(Optional.of(event));
        CompletableFuture<SendResult<String, String>> result = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send("risk.decisions.test", event.id().toString(), event.payload()))
                .thenReturn(result);

        service.publish(event.id());

        assertThat(event.isPending()).isFalse();
        assertThat(event.publishedAt()).isEqualTo(NOW);
        assertThat(meterRegistry.counter("sentinel.outbox.published").count()).isEqualTo(1);
    }

    @Test
    void retainsAFailedEventForRetry() {
        OutboxEventEntity event = event();
        when(repository.findById(event.id())).thenReturn(Optional.of(event));
        CompletableFuture<SendResult<String, String>> result =
                CompletableFuture.failedFuture(new IllegalStateException("broker unavailable"));
        when(kafkaTemplate.send("risk.decisions.test", event.id().toString(), event.payload()))
                .thenReturn(result);

        service.publish(event.id());

        assertThat(event.isPending()).isTrue();
        assertThat(event.attempts()).isEqualTo(1);
        assertThat(meterRegistry.counter("sentinel.outbox.failed").count()).isEqualTo(1);
    }

    @Test
    void ignoresMissingEvents() {
        UUID eventId = UUID.randomUUID();
        when(repository.findById(eventId)).thenReturn(Optional.empty());

        service.publish(eventId);

        verify(kafkaTemplate, never()).send(
                "risk.decisions.test", eventId.toString(), "payload"
        );
    }

    private OutboxEventEntity event() {
        return new OutboxEventEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "risk.decision.v1",
                "{\"decision\":\"APPROVED\"}",
                NOW.minusSeconds(10)
        );
    }
}
