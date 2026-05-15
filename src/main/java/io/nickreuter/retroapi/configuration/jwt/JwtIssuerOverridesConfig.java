package io.nickreuter.retroapi.configuration.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("jwt")
public record JwtIssuerOverridesConfig(Map<String, String> issuerOverrides) {
    public JwtIssuerOverridesConfig {
        if (issuerOverrides == null) {
            issuerOverrides = Map.of();
        }
    }
}
