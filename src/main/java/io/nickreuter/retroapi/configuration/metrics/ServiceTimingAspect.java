package io.nickreuter.retroapi.configuration.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceTimingAspect {

    private final MeterRegistry registry;

    public ServiceTimingAspect(MeterRegistry registry) {
        this.registry = registry;
    }

    @Around("@within(org.springframework.stereotype.Service) "
            + "&& within(io.nickreuter.retroapi..*) "
            + "&& !@annotation(io.micrometer.core.annotation.Timed)")
    public Object timeServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        var signature = (MethodSignature) joinPoint.getSignature();
        var sample = Timer.start(registry);
        try {
            return joinPoint.proceed();
        } finally {
            sample.stop(Timer.builder("retro.service")
                    .tag("class", signature.getDeclaringType().getSimpleName())
                    .tag("method", signature.getName())
                    .register(registry));
        }
    }
}
