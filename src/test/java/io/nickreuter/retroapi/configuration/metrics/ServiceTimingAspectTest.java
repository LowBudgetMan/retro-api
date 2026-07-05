package io.nickreuter.retroapi.configuration.metrics;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceTimingAspectTest {

    @Service
    static class SampleService {
        public String doWork() { return "done"; }

        @Timed("sample.custom")
        public String doTimedWork() { return "timed"; }

        public void doFailingWork() { throw new IllegalStateException("boom"); }
    }

    private SampleService proxyFor(SimpleMeterRegistry registry) {
        var factory = new AspectJProxyFactory(new SampleService());
        factory.addAspect(new ServiceTimingAspect(registry));
        return factory.getProxy();
    }

    @Test
    void timesPlainServiceMethod() {
        var registry = new SimpleMeterRegistry();
        var proxy = proxyFor(registry);

        proxy.doWork();

        var timer = registry.get("retro.service")
                .tag("class", "SampleService")
                .tag("method", "doWork")
                .tag("exception", "none")
                .timer();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void tagsThrownExceptionWithItsSimpleName() {
        var registry = new SimpleMeterRegistry();
        var proxy = proxyFor(registry);

        assertThatThrownBy(proxy::doFailingWork).isInstanceOf(IllegalStateException.class);

        var timer = registry.get("retro.service")
                .tag("method", "doFailingWork")
                .tag("exception", "IllegalStateException")
                .timer();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void doesNotDoubleCountTimedAnnotatedMethod() {
        var registry = new SimpleMeterRegistry();
        var proxy = proxyFor(registry);

        proxy.doTimedWork();

        assertThat(registry.find("retro.service").timer()).isNull();
    }
}
