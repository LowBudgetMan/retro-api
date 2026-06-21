package io.nickreuter.retroapi.configuration.metrics;

import io.micrometer.registry.otlp.OtlpMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MetricsExportDisabledByDefaultTest {

    @Autowired
    private ObjectProvider<OtlpMeterRegistry> otlpRegistry;

    @Test
    void otlpRegistryIsNotCreatedWithDefaultConfig() {
        assertThat(otlpRegistry.getIfAvailable()).isNull();
    }
}
