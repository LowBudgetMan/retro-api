package io.nickreuter.retroapi.configuration.environment;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("web.cors")
public record CorsConfig(
        List<String> allowedOrigins
) {
}
