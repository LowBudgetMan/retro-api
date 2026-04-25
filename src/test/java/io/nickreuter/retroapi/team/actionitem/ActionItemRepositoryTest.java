package io.nickreuter.retroapi.team.actionitem;

import io.nickreuter.retroapi.team.TeamEntity;
import io.nickreuter.retroapi.team.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
class ActionItemRepositoryTest {
    @Autowired
    private ActionItemRepository subject;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TestEntityManager entityManager;

    private ActionItemEntity completedActionItem;
    private ActionItemEntity archivedActionItem;
    private ActionItemEntity incompleteActionItem;
    private ActionItemEntity differentTeamActionItem;
    private TeamEntity team;

    @BeforeEach
    void setup() {
        team = teamRepository.saveAndFlush(new TeamEntity("test-team"));
        var team2 = teamRepository.saveAndFlush(new TeamEntity("test-team2"));
        completedActionItem = subject.saveAndFlush(new ActionItemEntity(null, "Do the thing", true, false, team.getId(), "me", Instant.now()));
        archivedActionItem = subject.saveAndFlush(new ActionItemEntity(null, "Do the thing", true, true, team.getId(), "me", Instant.now()));
        incompleteActionItem = subject.saveAndFlush(new ActionItemEntity(null, "Do the thing", false, false, team.getId(), "me", Instant.now()));
        differentTeamActionItem = subject.saveAndFlush(new ActionItemEntity(null, "Do the thing", true, false, team2.getId(), "me", Instant.now()));
    }

    @Test
    void archiveCompletedActionItemsForTeam_WhenActionItemIsCompleted_ShouldArchive() {
        subject.archiveCompletedActionItemsForTeam(team.getId());
        entityManager.clear();
        assertThat(subject.findById(completedActionItem.getId()).orElseThrow().isArchived()).isTrue();
    }

    @Test
    void archiveCompletedActionItemsForTeam_WhenActionItemIsNotComplete_ShouldNotArchive() {
        subject.archiveCompletedActionItemsForTeam(team.getId());
        entityManager.clear();
        assertThat(subject.findById(incompleteActionItem.getId()).orElseThrow().isArchived()).isFalse();
    }

    @Test
    void archiveCompletedActionItemsForTeam_WhenActionItemIsOnDifferentTeam_ShouldNotArchive() {
        subject.archiveCompletedActionItemsForTeam(team.getId());
        entityManager.clear();
        assertThat(subject.findById(differentTeamActionItem.getId()).orElseThrow().isArchived()).isFalse();
    }

    @Test
    void archiveCompletedActionItemsForTeam_WhenActionItemIsAlreadyArchived_ShouldKeepArchived() {
        subject.archiveCompletedActionItemsForTeam(team.getId());
        entityManager.clear();
        assertThat(subject.findById(archivedActionItem.getId()).orElseThrow().isArchived()).isTrue();
    }
}