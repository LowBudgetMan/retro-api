package io.nickreuter.retroapi.retro;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetroServiceTest {
    private final RetroRepository retroRepository = mock(RetroRepository.class);
    private final RetroService subject = new RetroService(retroRepository);

    @Test
    void createRetro_ReturnsCreatedRetro() {
        var teamId = UUID.randomUUID();
        var expected = new RetroEntity(UUID.randomUUID(), teamId, Instant.now());
        when(retroRepository.save(ArgumentMatchers.argThat((RetroEntity retro) ->
            retro.getId() == null &&
            Objects.equals(retro.getTeamId(), teamId) &&
            retro.getCreatedAt() == null))
        ).thenReturn(expected);

        var actual = subject.createRetro(teamId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getRetros_ReturnsRetros() {
        var teamId = UUID.randomUUID();
        var retro1 = new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        var retro2 = new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        var expected = List.of(retro1, retro2);
        when(retroRepository.findAllByTeamIdOrderByCreatedAtDesc(teamId)).thenReturn(expected);

        var actual = subject.getRetros(teamId);

        assertThat(actual).isEqualTo(expected);
    }
}