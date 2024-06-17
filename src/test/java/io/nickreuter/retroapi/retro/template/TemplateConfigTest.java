package io.nickreuter.retroapi.retro.template;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TemplateConfigTest {
    private final ApplicationContext applicationContext = mock(ApplicationContext.class);
    private final TemplateConfig subject = new TemplateConfig(applicationContext);

    @Test
    void templates_ReturnsListOfTemplatesWhenYamlFileValid() throws Exception {
        when(applicationContext.getResources("classpath:templates/*")).thenReturn(new Resource[]{
                new ClassPathResource("templates/valid-3-column.yml"),
                new ClassPathResource("templates/valid-1-column.yml")
        });
        assertThat(subject.templates()).containsExactly(
                new Template(
                    "valid-3-column.yml",
                    "Happy, Confused, Sad",
                    "Description",
                    List.of(
                        new Category("Happy", 1, "#ffffff", "#ffffff", "#ffffff", "#ffffff"),
                        new Category("Confused", 2, "#ffffff", "#ffffff", "#ffffff", "#ffffff"),
                        new Category("Sad", 3, "#ffffff", "#ffffff", "#ffffff", "#ffffff")
                    )
                ),
                new Template(
                        "valid-1-column.yml",
                        "Happy",
                        "Description",
                        List.of(new Category("Happy", 1, "#ffffff", "#ffffff", "#ffffff", "#ffffff"))
                )
        );
    }

    @Test
    void templates_WhenTemplateInFolderIsInvalid_IgnoresTemplate() throws Exception {
        when(applicationContext.getResources("classpath:templates/*")).thenReturn(new Resource[]{
                new ClassPathResource("templates/valid-3-column.yml"),
                new ClassPathResource("templates/invalid.yml")
        });
        assertThat(subject.templates()).containsExactly(
                new Template(
                        "valid-3-column.yml",
                        "Happy, Confused, Sad",
                        "Description",
                        List.of(
                                new Category("Happy", 1, "#ffffff", "#ffffff", "#ffffff", "#ffffff"),
                                new Category("Confused", 2, "#ffffff", "#ffffff", "#ffffff", "#ffffff"),
                                new Category("Sad", 3, "#ffffff", "#ffffff", "#ffffff", "#ffffff")
                        )
                )
        );
    }

    @Test
    void templates_WhenGettingTemplatesThrowsAnIOException_ReturnsAnEmptyList() throws Exception {
        when(applicationContext.getResources("classpath:templates/*")).thenThrow(IOException.class);
        assertThat(subject.templates()).isEmpty();
    }
}