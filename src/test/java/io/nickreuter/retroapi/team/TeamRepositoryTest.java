package io.nickreuter.retroapi.team;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TeamRepositoryTest {
    @Autowired
    private TeamRepository subject;

    @Test
    public void existsByName_WhenTeamNameDoesNotExist_ReturnsFalse() {
        assertFalse(subject.existsByName("name"));
    }

    @Test
    public void existsByName_WhenTeamNameExists_ReturnsTrue() {
        subject.save(new TeamEntity("name"));
        assertTrue(subject.existsByName("name"));
    }
}