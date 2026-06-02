package io.nickreuter.retroapi.configuration;

import io.nickreuter.retroapi.notification.EventType;
import io.nickreuter.retroapi.notification.event.BaseEvent;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import jakarta.persistence.Entity;

@Configuration
@ImportRuntimeHints(JacksonNativeHints.Registrar.class)
class JacksonNativeHints {

    private static final String BASE_PACKAGE = "io.nickreuter.retroapi";
    private static final MemberCategory[] JACKSON_CATEGORIES = {
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS
    };

    static class Registrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            var scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(BaseEvent.class));
            scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

            for (BeanDefinition bd : scanner.findCandidateComponents(BASE_PACKAGE)) {
                try {
                    hints.reflection().registerType(
                            Class.forName(bd.getBeanClassName()),
                            JACKSON_CATEGORIES
                    );
                } catch (ClassNotFoundException ignored) {
                }
            }

            hints.reflection().registerType(EventType.class, JACKSON_CATEGORIES);
        }
    }
}
