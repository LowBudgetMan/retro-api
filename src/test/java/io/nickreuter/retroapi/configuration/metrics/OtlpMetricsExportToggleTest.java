package io.nickreuter.retroapi.configuration.metrics;

import io.micrometer.registry.otlp.OtlpMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.otlp.OtlpMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpMetricsExportToggleTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MetricsAutoConfiguration.class,
                    SimpleMetricsExportAutoConfiguration.class,
                    OtlpMetricsExportAutoConfiguration.class));

    @Test
    void otlpRegistryAbsentWhenExportDisabled() {
        runner.withPropertyValues("management.otlp.metrics.export.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(OtlpMeterRegistry.class));
    }

    @Test
    void otlpRegistryPresentWhenExportEnabled() {
        runner.withPropertyValues("management.otlp.metrics.export.enabled=true")
                .run(ctx -> assertThat(ctx).hasSingleBean(OtlpMeterRegistry.class));
    }
}
