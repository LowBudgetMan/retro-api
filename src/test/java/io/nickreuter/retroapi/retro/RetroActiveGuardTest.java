package io.nickreuter.retroapi.retro;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetroActiveGuardTest {
    private final RetroRepository mockRetroRepository = mock(RetroRepository.class);
    private final RetroActiveGuard subject = new RetroActiveGuard(mockRetroRepository);

    @Test
    void isValid_WhenRetroIsNotFinished_ReturnsTrue() throws RetroNotFoundException {
        var retroId = UUID.randomUUID();
        var savedRetro = new RetroEntity(retroId, UUID.randomUUID(), false, "some template", Set.of(), Instant.now());
        when(mockRetroRepository.findById(retroId)).thenReturn(Optional.of(savedRetro));
        assertThat(subject.isValid(retroId)).isTrue();
    }

    @Test
    void isValid_WhenRetroIsFinished_ReturnsFalse() throws RetroNotFoundException {
        var retroId = UUID.randomUUID();
        var savedRetro = new RetroEntity(retroId, UUID.randomUUID(), true, "some template", Set.of(), Instant.now());
        when(mockRetroRepository.findById(retroId)).thenReturn(Optional.of(savedRetro));
        assertThat(subject.isValid(retroId)).isFalse();
    }

    @Test
    void isValid_WhenRetroIsNotFound_ThrowsRetroNotFoundException() {
        var retroId = UUID.randomUUID();
        when(mockRetroRepository.findById(retroId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subject.isValid(retroId)).isInstanceOf(RetroNotFoundException.class);
    }
}