package io.nickreuter.retroapi.configuration;

import liquibase.datatype.LiquibaseDataType;
import liquibase.snapshot.jvm.JdbcSnapshotGenerator;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.type.filter.AssignableTypeFilter;

@Configuration
@ImportRuntimeHints(LiquibaseNativeHints.Registrar.class)
class LiquibaseNativeHints {

    private static final MemberCategory[] CATEGORIES = {
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS
    };

    static class Registrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources().registerPattern("db/*");
            hints.resources().registerPattern("www.liquibase.org/*");
            hints.resources().registerPattern("liquibase/*");

            var scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(JdbcSnapshotGenerator.class));
            scanner.addIncludeFilter(new AssignableTypeFilter(LiquibaseDataType.class));

            for (BeanDefinition bd : scanner.findCandidateComponents("liquibase")) {
                try {
                    hints.reflection().registerType(Class.forName(bd.getBeanClassName()), CATEGORIES);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
    }
}
