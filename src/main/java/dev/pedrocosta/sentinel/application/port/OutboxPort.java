package dev.pedrocosta.sentinel.application.port;

import dev.pedrocosta.sentinel.application.event.RiskDecisionEvent;

public interface OutboxPort {

    void append(RiskDecisionEvent event);
}
