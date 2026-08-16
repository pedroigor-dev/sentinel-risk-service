package dev.pedrocosta.sentinel.infrastructure.outbox;

import dev.pedrocosta.sentinel.application.event.RiskDecisionEvent;
import dev.pedrocosta.sentinel.application.port.OutboxPort;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
public class JpaOutboxAdapter implements OutboxPort {

    private static final String EVENT_TYPE = "risk.decision.v1";

    private final OutboxEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    public JpaOutboxAdapter(OutboxEventJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void append(RiskDecisionEvent event) {
        repository.save(new OutboxEventEntity(
                event.eventId(),
                event.analysisId(),
                EVENT_TYPE,
                serialize(event),
                event.analyzedAt()
        ));
    }

    private String serialize(RiskDecisionEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize the risk decision event", exception);
        }
    }
}
