package dev.pedrocosta.sentinel.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentinel.security")
public record ApiKeyProperties(String apiKey) {

    public ApiKeyProperties {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("sentinel.security.api-key is required");
        }
    }
}
