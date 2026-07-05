package io.nickreuter.retroapi.configuration.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the safety property: the shipped production application.yml must keep OTLP metrics
 * export disabled by default (no surprise metric traffic, no connection-refused log spam).
 *
 * <p>Note: this asserts against the real {@code src/main/resources/application.yml} on disk
 * rather than via {@code @SpringBootTest}, because {@code src/test/resources/application.yml}
 * shadows the production file on the test classpath — a boot-based test would never see the
 * production value. The toggle <em>mechanism</em> (property true/false -> registry present/absent)
 * is covered separately by {@link OtlpMetricsExportToggleTest}.
 */
class MetricsExportDisabledByDefaultTest {

    @Test
    void shippedApplicationYamlDisablesOtlpExportByDefault() throws Exception {
        Resource resource = new FileSystemResource("src/main/resources/application.yml");
        assertThat(resource.exists())
                .as("production application.yml should be present at the expected path")
                .isTrue();

        List<PropertySource<?>> sources = new YamlPropertySourceLoader().load("application.yml", resource);
        Object enabled = sources.stream()
                .map(source -> source.getProperty("management.otlp.metrics.export.enabled"))
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);

        assertThat(enabled)
                .as("management.otlp.metrics.export.enabled must be false in the shipped application.yml")
                .isEqualTo(false);
    }
}
