package dev.pedrocosta.sentinel.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalysisCommandTest {

    @Test
    void normalizesCodesAndIdentifiers() {
        AnalysisCommand command = command(" tx-42 ", new BigDecimal("125.50"), "br", "us");

        assertThat(command.transactionId()).isEqualTo("tx-42");
        assertThat(command.currency()).isEqualTo("BRL");
        assertThat(command.originCountry()).isEqualTo("BR");
        assertThat(command.cardCountry()).isEqualTo("US");
    }

    @Test
    void rejectsNonPositiveAmounts() {
        assertThatThrownBy(() -> command("tx-42", BigDecimal.ZERO, "BR", "BR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be positive");
    }

    @Test
    void rejectsMalformedCurrencyCodes() {
        assertThatThrownBy(() -> new AnalysisCommand(
                "tx-42", "customer-7", BigDecimal.TEN, "REAL", "BR", "BR", "5411",
                Instant.parse("2026-08-16T12:00:00Z")
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("currency must have 3 characters");
    }

    private AnalysisCommand command(
            String transactionId,
            BigDecimal amount,
            String originCountry,
            String cardCountry
    ) {
        return new AnalysisCommand(
                transactionId,
                "customer-7",
                amount,
                "brl",
                originCountry,
                cardCountry,
                "5411",
                Instant.parse("2026-08-16T12:00:00Z")
        );
    }
}
