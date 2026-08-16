package dev.pedrocosta.sentinel.presentation.api;

import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalyzeTransactionRequest(
        @NotBlank @Size(max = 100) String transactionId,
        @NotBlank @Size(max = 100) String customerId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String originCountry,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String cardCountry,
        @NotBlank @Pattern(regexp = "[0-9]{4}") String merchantCategory,
        @NotNull Instant occurredAt
) {

    AnalysisCommand toCommand() {
        return new AnalysisCommand(
                transactionId,
                customerId,
                amount,
                currency,
                originCountry,
                cardCountry,
                merchantCategory,
                occurredAt
        );
    }
}
