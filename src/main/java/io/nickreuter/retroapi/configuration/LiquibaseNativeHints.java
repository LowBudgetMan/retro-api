package io.nickreuter.retroapi.configuration;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(LiquibaseNativeHints.Registrar.class)
class LiquibaseNativeHints {

    static class Registrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources().registerPattern("db/.*");
            hints.resources().registerPattern("www.liquibase.org/.*");
            hints.resources().registerPattern("liquibase/.*");
        }
    }
}
