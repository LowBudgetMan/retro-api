package io.nickreuter.retroapi.configuration.environment;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("websocket")
public record WebsocketEnvironmentConfig(
        @NotNull
        String baseUrl
) {}
