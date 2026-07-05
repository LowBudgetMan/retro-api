package io.nickreuter.retroapi.configuration.metrics;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsConfigTest {

    private final MetricsConfig config = new MetricsConfig();

    static class TimedBean {
        @Timed("test.timed.work")
        public void doTimedWork() {}
    }

    @Test
    void timedAspectRecordsNamedTimer() {
        var registry = new SimpleMeterRegistry();
        var factory = new AspectJProxyFactory(new TimedBean());
        factory.addAspect(config.timedAspect(registry));
        TimedBean proxy = factory.getProxy();

        proxy.doTimedWork();

        assertThat(registry.get("test.timed.work").timer().count()).isEqualTo(1);
    }

    @Test
    void commonTagsCustomizerAppliesApplicationTag() {
        var registry = new SimpleMeterRegistry();
        config.commonTagsCustomizer().customize(registry);

        registry.counter("anything").increment();

        assertThat(registry.get("anything").counter().getId().getTag("application"))
                .isEqualTo("retro-api");
    }
}
