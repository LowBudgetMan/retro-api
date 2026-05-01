package io.nickreuter.retroapi.team.apitoken;

import io.nickreuter.retroapi.team.TeamEntity;
import io.nickreuter.retroapi.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ApiTokenRepositoryTest {
    @Autowired
    private ApiTokenRepository subject;
    @Autowired
    private TeamRepository teamRepository;

    @Test
    void findByTokenHash_WhenTokenExists_ReturnsToken() {
        var team = teamRepository.save(new TeamEntity(null, "Team", Instant.now()));
        var saved = subject.save(new ApiTokenEntity(null, team.getId(), "Slack", "hash-abc", "retro_pat_abcd", "read", null, "user-1", null, null));

        assertThat(subject.findByTokenHash("hash-abc")).contains(saved);
    }

    @Test
    void findByTokenHash_WhenTokenMissing_ReturnsEmpty() {
        assertThat(subject.findByTokenHash("nope")).isEmpty();
    }

    @Test
    void findAllByTeamId_ReturnsTokensForThatTeamOnly() {
        var teamA = teamRepository.save(new TeamEntity(null, "A", Instant.now()));
        var teamB = teamRepository.save(new TeamEntity(null, "B", Instant.now()));
        var inA = subject.save(new ApiTokenEntity(null, teamA.getId(), "Token A", "hash-a", "retro_pat_aaaa", "read", null, "user-1", null, null));
        subject.save(new ApiTokenEntity(null, teamB.getId(), "Token B", "hash-b", "retro_pat_bbbb", "read", null, "user-1", null, null));

        assertThat(subject.findAllByTeamId(teamA.getId())).containsExactly(inA);
    }
}
