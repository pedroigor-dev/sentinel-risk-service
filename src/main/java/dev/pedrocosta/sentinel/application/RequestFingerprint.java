package dev.pedrocosta.sentinel.application;

import dev.pedrocosta.sentinel.domain.model.AnalysisCommand;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class RequestFingerprint {

    public String from(AnalysisCommand command) {
        String canonicalRequest = String.join("|",
                command.transactionId(),
                command.customerId(),
                command.amount().toPlainString(),
                command.currency(),
                command.originCountry(),
                command.cardCountry(),
                command.merchantCategory(),
                command.occurredAt().toString()
        );
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(
                    canonicalRequest.getBytes(StandardCharsets.UTF_8)
            ));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
