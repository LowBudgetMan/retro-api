package io.nickreuter.retroapi.retro.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Configuration
public class TemplateConfig {

    private static final String TEMPLATES_LOCATION = "classpath:templates/*";
    private final ApplicationContext applicationContext;
    private final Logger logger = LoggerFactory.getLogger(TemplateConfig.class);

    public TemplateConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public List<Template> templates() {
        try {
            var resources = applicationContext.getResources(TEMPLATES_LOCATION);
            return Arrays.stream(resources)
                    .map(this::parseTemplate)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private Template parseTemplate(Resource resource) {
        try {
            return new ObjectMapper(new YAMLFactory()).readValue(resource.getContentAsString(StandardCharsets.UTF_8), Template.class);
        } catch (IOException e) {
            logger.error("Failed to parse template %s".formatted(resource.getFilename()), e);
            return null;
        }
    }
}
