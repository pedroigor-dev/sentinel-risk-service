package dev.pedrocosta.sentinel.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "sentinel.outbox")
public record OutboxProperties(String topic, Duration sendTimeout) {

    public OutboxProperties {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("sentinel.outbox.topic is required");
        }
        if (sendTimeout == null || sendTimeout.isNegative() || sendTimeout.isZero()) {
            throw new IllegalArgumentException("sentinel.outbox.send-timeout must be positive");
        }
    }
}
