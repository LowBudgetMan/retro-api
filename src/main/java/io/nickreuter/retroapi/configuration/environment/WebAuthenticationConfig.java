package io.nickreuter.retroapi.configuration.environment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("web.authentication")
public record WebAuthenticationConfig(
        String authority,
        String clientId
) {}
