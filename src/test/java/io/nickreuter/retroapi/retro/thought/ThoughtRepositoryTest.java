package io.nickreuter.retroapi.retro.thought;

import io.nickreuter.retroapi.retro.RetroEntity;
import io.nickreuter.retroapi.retro.RetroRepository;
import io.nickreuter.retroapi.team.TeamEntity;
import io.nickreuter.retroapi.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ThoughtRepositoryTest {
    @Autowired
    private ThoughtRepository subject;
    @Autowired
    private RetroRepository retroRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Test
    void save_ShouldPreserveEmojiCharacters() {
        var team = teamRepository.save(new TeamEntity("test-team"));
        var retro = retroRepository.save(new RetroEntity(team.getId(), "template-1"));
        var messageWithEmoji = "Great work! 😀🎉👍 Keep it up! 🚀";

        var thought = ThoughtEntity.from(messageWithEmoji, "happy", retro.getId());
        var saved = subject.save(thought);

        var retrieved = subject.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getMessage()).isEqualTo(messageWithEmoji);
    }
}
