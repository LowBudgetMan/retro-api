package io.nickreuter.retroapi.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("broker.relay")
public record BrokerRelayProperties(
        String relayHost,
        Integer relayPort,
        String relayUsername,
        String relayPassword
) {
    public boolean isConfigured() {
        return relayHost != null && relayPort != null && relayUsername != null && relayPassword != null;
    }
}
