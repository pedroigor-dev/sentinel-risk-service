package dev.pedrocosta.sentinel.infrastructure.outbox;

import dev.pedrocosta.sentinel.configuration.OutboxProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OutboxPublicationService {

    private final OutboxEventJpaRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxProperties properties;
    private final Clock clock;
    private final Counter publishedCounter;
    private final Counter failedCounter;

    public OutboxPublicationService(
            OutboxEventJpaRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            OutboxProperties properties,
            Clock clock,
            MeterRegistry meterRegistry
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.clock = clock;
        this.publishedCounter = meterRegistry.counter("sentinel.outbox.published");
        this.failedCounter = meterRegistry.counter("sentinel.outbox.failed");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(UUID eventId) {
        OutboxEventEntity event = repository.findById(eventId).orElse(null);
        if (event == null || !event.isPending()) {
            return;
        }

        try {
            kafkaTemplate.send(properties.topic(), event.id().toString(), event.payload())
                    .get(properties.sendTimeout().toMillis(), TimeUnit.MILLISECONDS);
            event.markPublished(Instant.now(clock));
            publishedCounter.increment();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            recordFailure(event, exception);
        } catch (Exception exception) {
            recordFailure(event, exception);
        }
    }

    private void recordFailure(OutboxEventEntity event, Exception exception) {
        event.recordFailure(exception.getMessage());
        failedCounter.increment();
    }
}
