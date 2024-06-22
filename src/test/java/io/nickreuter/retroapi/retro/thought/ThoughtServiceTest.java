package io.nickreuter.retroapi.retro.thought;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThoughtServiceTest {
    private final ThoughtRepository thoughtRepository = mock(ThoughtRepository.class);
    private final ThoughtService subject = new ThoughtService(thoughtRepository);

    @Test
    void createThought_SavesThoughtInRepository() {
        var message = "message";
        var category = "category";
        var retroId = UUID.randomUUID();
        var expected = ThoughtEntity.from(message, category, retroId);
        when(thoughtRepository.save(ArgumentMatchers.argThat(entity ->
                entity.getId() == null &&
                Objects.equals(entity.getMessage(), message) &&
                entity.getVotes() == 0 &&
                !entity.isCompleted() &&
                Objects.equals(entity.getCategory(), category) &&
                entity.getRetroId() == retroId &&
                entity.getCreatedAt() == null))
        ).thenReturn(expected);

        var actual = subject.createThought(retroId, message, category);

        assertThat(actual).isEqualTo(expected);
    }
}