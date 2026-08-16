package dev.pedrocosta.sentinel.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public record AnalysisCommand(
        String transactionId,
        String customerId,
        BigDecimal amount,
        String currency,
        String originCountry,
        String cardCountry,
        String merchantCategory,
        Instant occurredAt
) {

    public AnalysisCommand {
        transactionId = requireText(transactionId, "transactionId");
        customerId = requireText(customerId, "customerId");
        Objects.requireNonNull(amount, "amount is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        amount = amount.setScale(2);
        currency = normalizeCode(currency, 3, "currency");
        originCountry = normalizeCode(originCountry, 2, "originCountry");
        cardCountry = normalizeCode(cardCountry, 2, "cardCountry");
        merchantCategory = normalizeCode(merchantCategory, 4, "merchantCategory");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    private static String normalizeCode(String value, int length, String field) {
        String normalized = requireText(value, field).toUpperCase(Locale.ROOT);
        if (normalized.length() != length) {
            throw new IllegalArgumentException(field + " must have " + length + " characters");
        }
        return normalized;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
