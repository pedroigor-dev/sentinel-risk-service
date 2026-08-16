package dev.pedrocosta.sentinel.infrastructure.outbox;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxRelayTest {

    @Test
    void relaysPendingEventsInCreationOrder() {
        OutboxEventJpaRepository repository = mock(OutboxEventJpaRepository.class);
        OutboxPublicationService publicationService = mock(OutboxPublicationService.class);
        OutboxEventEntity first = event(Instant.parse("2026-08-16T12:00:00Z"));
        OutboxEventEntity second = event(Instant.parse("2026-08-16T12:01:00Z"));
        when(repository.findTop50ByStatusOrderByCreatedAt(OutboxStatus.PENDING))
                .thenReturn(List.of(first, second));
        OutboxRelay relay = new OutboxRelay(repository, publicationService);

        assertThat(relay.pendingEventIds()).containsExactly(first.id(), second.id());
        relay.relayPendingEvents();

        verify(publicationService).publish(first.id());
        verify(publicationService).publish(second.id());
    }

    private OutboxEventEntity event(Instant createdAt) {
        return new OutboxEventEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "risk.decision.v1",
                "{}",
                createdAt
        );
    }
}
