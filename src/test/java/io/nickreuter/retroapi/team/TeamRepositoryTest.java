package io.nickreuter.retroapi.team;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TeamRepositoryTest {
    @Autowired
    private TeamRepository subject;

    @Test
    public void findAllByIdInOrderByNameAsc_WhenTeamsExist_ReturnsTeamsInNameOrderAscending() {
        var team1 = subject.save(new TeamEntity("C Team"));
        var team2 = subject.save(new TeamEntity("A Team"));
        subject.save(new TeamEntity("B Team"));

        var actual = subject.findAllByIdInOrderByNameAsc(Set.of(team1.getId(), team2.getId()));

        assertThat(actual).containsExactly(team2, team1);
    }

    @Test
    public void findAllByIdInOrderByNameAsc_WhenNoTeamsExist_ReturnsEmptyList() {
        var actual = subject.findAllByIdInOrderByNameAsc(Set.of(UUID.randomUUID(), UUID.randomUUID()));
        assertThat(actual).isEmpty();
    }
}