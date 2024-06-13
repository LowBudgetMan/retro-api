package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.template.Category;
import io.nickreuter.retroapi.retro.template.Template;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetroServiceTest {
    private final RetroRepository retroRepository = mock(RetroRepository.class);
    private final RetroService subject = new RetroService(retroRepository, List.of(new Template(0, "name", "description", List.of(new Category("column", 1, "", "", "", "")))));

    @Test
    void createRetro_ReturnsCreatedRetro() throws InvalidTemplateIdException {
        var teamId = UUID.randomUUID();
        var expected = new RetroEntity(UUID.randomUUID(), teamId, false, 0, Instant.now());
        when(retroRepository.save(ArgumentMatchers.argThat((RetroEntity retro) ->
            retro.getId() == null &&
            Objects.equals(retro.getTeamId(), teamId) &&
            retro.getCreatedAt() == null))
        ).thenReturn(expected);

        var actual = subject.createRetro(teamId, 0);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createRetro_WhenTemplateIdIsNull_ThrowsInvalidTemplateIdException() {
        assertThatThrownBy(() -> subject.createRetro(UUID.randomUUID(), null)).isInstanceOf(InvalidTemplateIdException.class);
    }

    @Test
    void createRetro_WhenTemplateIdDoesNotExist_ThrowsInvalidTemplateIdException() {
        assertThatThrownBy(() -> subject.createRetro(UUID.randomUUID(), 2)).isInstanceOf(InvalidTemplateIdException.class);
    }

    @Test
    void getRetros_ReturnsRetros() {
        var teamId = UUID.randomUUID();
        var retro1 = new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, 0, Instant.now());
        var retro2 = new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, 0, Instant.now());
        var expected = List.of(retro1, retro2);
        when(retroRepository.findAllByTeamIdOrderByCreatedAtDesc(teamId)).thenReturn(expected);

        var actual = subject.getRetros(teamId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getRetro_ReturnsRetro() {
        var retroId = UUID.randomUUID();
        var expected = new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, 0, Instant.now());
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(expected));

        assertThat(subject.getRetro(retroId)).contains(expected);
    }
}