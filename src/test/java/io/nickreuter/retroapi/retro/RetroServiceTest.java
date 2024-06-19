package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.template.Category;
import io.nickreuter.retroapi.retro.template.Template;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RetroServiceTest {
    private final RetroRepository retroRepository = mock(RetroRepository.class);
    private final Template savedTemplate = new Template("template", "name", "description", List.of(new Category("column", 1, "", "", "", "")));
    private final RetroService subject = new RetroService(retroRepository, List.of(savedTemplate));

    @Test
    void createRetro_ReturnsCreatedRetro() throws InvalidTemplateIdException {
        var teamId = UUID.randomUUID();
        var expected = new RetroEntity(UUID.randomUUID(), teamId, false, "template", Set.of(), Instant.now());
        when(retroRepository.save(ArgumentMatchers.argThat((RetroEntity retro) ->
            retro.getId() == null &&
            Objects.equals(retro.getTeamId(), teamId) &&
            retro.getCreatedAt() == null))
        ).thenReturn(expected);

        var actual = subject.createRetro(teamId, "template");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createRetro_WhenTemplateIdIsNull_ThrowsInvalidTemplateIdException() {
        assertThatThrownBy(() -> subject.createRetro(UUID.randomUUID(), null)).isInstanceOf(InvalidTemplateIdException.class);
    }

    @Test
    void createRetro_WhenTemplateIdDoesNotExist_ThrowsInvalidTemplateIdException() {
        assertThatThrownBy(() -> subject.createRetro(UUID.randomUUID(), "template2")).isInstanceOf(InvalidTemplateIdException.class);
    }

    @Test
    void getRetros_ReturnsRetros() {
        var teamId = UUID.randomUUID();
        var retro1 = new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now());
        var retro2 = new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now());
        var expected = List.of(retro1, retro2);
        when(retroRepository.findAllByTeamIdOrderByCreatedAtDesc(teamId)).thenReturn(expected);

        var actual = subject.getRetros(teamId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getRetro_ReturnsRetro() {
        var retroId = UUID.randomUUID();
        var savedEntity = new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now());
        var expected = Retro.from(savedEntity, savedTemplate);
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(savedEntity));

        assertThat(subject.getRetro(retroId)).contains(expected);
    }

    @Test
    void setFinished_UpdatesRetro() throws RetroNotFoundException {
        var retroId = UUID.randomUUID();
        var savedRetro = new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now());
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(savedRetro));

        subject.setFinished(retroId, true);

        var captor = ArgumentCaptor.forClass(RetroEntity.class);
        verify(retroRepository).save(captor.capture());
        assertThat(captor.getValue().isFinished()).isTrue();
    }

    @Test
    void setFinished_WhenRetroNotFound_ThrowsRetroNotFoundException() {
        var retroId = UUID.randomUUID();
        when(retroRepository.findById(retroId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subject.setFinished(retroId, true)).isInstanceOf(RetroNotFoundException.class);
    }
}