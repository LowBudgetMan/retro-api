package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.thought.ThoughtEntity;
import io.nickreuter.retroapi.retro.thought.ThoughtRepository;
import io.nickreuter.retroapi.team.TeamEntity;
import io.nickreuter.retroapi.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RetroDeletionIntegrationTest {
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private RetroService subject;
    @Autowired
    private RetroRepository retroRepository;
    @Autowired
    private ThoughtRepository thoughtRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Test
    void deleteRetro_RemovesRetroAndItsThoughts() {
        var team = teamRepository.saveAndFlush(new TeamEntity("test-team"));
        var retro = retroRepository.saveAndFlush(new RetroEntity(team.getId(), "happy-confused-sad.yml"));
        var thought = thoughtRepository.saveAndFlush(
                new ThoughtEntity(null, "a thought", 0, false, "Happy", retro.getId(), null));

        subject.deleteRetro(retro.getId());

        assertThat(retroRepository.findById(retro.getId())).isEmpty();
        assertThat(thoughtRepository.findById(thought.getId())).isEmpty();
    }
}
