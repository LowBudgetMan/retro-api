package io.nickreuter.retroapi.configuration.environment;

import org.springframework.stereotype.Component;

@Component
public record EnvironmentConfiguration (
        WebAuthenticationConfig webAuthentication
){}
