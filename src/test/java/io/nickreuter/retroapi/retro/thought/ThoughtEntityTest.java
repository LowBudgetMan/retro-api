package io.nickreuter.retroapi.retro.thought;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtEntityTest {

    @Test
    void from_CreatesThoughtWithDefaults() {
        var message = "message";
        var category = "category";
        var retroId = UUID.randomUUID();

        var actual = ThoughtEntity.from(message, category, retroId);

        assertThat(actual.getId()).isNull();
        assertThat(actual.getMessage()).isEqualTo(message);
        assertThat(actual.getVotes()).isEqualTo(0);
        assertThat(actual.isCompleted()).isFalse();
        assertThat(actual.getCategory()).isEqualTo(category);
        assertThat(actual.getRetroId()).isEqualTo(retroId);
        assertThat(actual.getCreatedAt()).isNull();
    }
}