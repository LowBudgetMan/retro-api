package io.nickreuter.retroapi.configuration.environment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuration")
public class EnvironmentConfigurationController {
    private final EnvironmentConfiguration environmentConfiguration;

    public EnvironmentConfigurationController(EnvironmentConfiguration environmentConfiguration) {
        this.environmentConfiguration = environmentConfiguration;
    }

    @GetMapping
    public EnvironmentConfiguration getConfiguration() {
        return environmentConfiguration;
    }
}
