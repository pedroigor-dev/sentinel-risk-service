package dev.pedrocosta.sentinel.infrastructure.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "sentinel.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {

    private final OutboxEventJpaRepository repository;
    private final OutboxPublicationService publicationService;

    public OutboxRelay(
            OutboxEventJpaRepository repository,
            OutboxPublicationService publicationService
    ) {
        this.repository = repository;
        this.publicationService = publicationService;
    }

    @Scheduled(fixedDelayString = "${sentinel.outbox.fixed-delay:1000}")
    public void relayPendingEvents() {
        pendingEventIds().forEach(publicationService::publish);
    }

    @Transactional(readOnly = true)
    List<UUID> pendingEventIds() {
        return repository.findTop50ByStatusOrderByCreatedAt(OutboxStatus.PENDING).stream()
                .map(OutboxEventEntity::id)
                .toList();
    }
}
