package dev.pedrocosta.sentinel.application.event;

import dev.pedrocosta.sentinel.domain.model.RiskDecision;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskDecisionEventTest {

    @Test
    void keepsAnImmutableSnapshotOfFactorCodes() {
        List<String> factorCodes = new ArrayList<>(List.of("HIGH_VELOCITY"));
        RiskDecisionEvent event = new RiskDecisionEvent(
                UUID.randomUUID(), UUID.randomUUID(), "txn-1", "customer-1",
                new BigDecimal("125.00"), "BRL", 30, RiskDecision.APPROVED,
                factorCodes, Instant.parse("2026-08-16T12:00:00Z"),
                Instant.parse("2026-08-16T12:00:01Z")
        );

        factorCodes.add("COUNTRY_MISMATCH");

        assertThat(event.factorCodes()).containsExactly("HIGH_VELOCITY");
        assertThatThrownBy(() -> event.factorCodes().add("UNUSUAL_HOUR"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
