package io.nickreuter.retroapi.configuration.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("jwt")
public record JwtIssuerOverridesConfig(List<IssuerOverride> issuerOverrides) {
    public JwtIssuerOverridesConfig {
        if (issuerOverrides == null) {
            issuerOverrides = List.of();
        }
    }

    public record IssuerOverride(String from, String to) {}
}
