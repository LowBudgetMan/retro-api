package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.team.TeamEntity;
import io.nickreuter.retroapi.team.TeamRepository;
import io.nickreuter.retroapi.team.actionitem.ActionItemEntity;
import io.nickreuter.retroapi.team.actionitem.ActionItemRepository;
import io.nickreuter.retroapi.team.subscription.Plan;
import io.nickreuter.retroapi.team.subscription.PlanStatus;
import io.nickreuter.retroapi.team.subscription.TeamSubscriptionEntity;
import io.nickreuter.retroapi.team.subscription.TeamSubscriptionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RetroFinishedListenerOrderingTest {
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private RetroService retroService;
    @Autowired
    private RetroRepository retroRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamSubscriptionRepository subscriptionRepository;
    @Autowired
    private ActionItemRepository actionItemRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void setFinished_WhenTeamLacksHistoricalRetros_ArchivesActionItemsBeforeDeletingRetro() throws RetroNotFoundException {
        var team = teamRepository.saveAndFlush(new TeamEntity("retention-team"));
        subscriptionRepository.saveAndFlush(
                new TeamSubscriptionEntity(null, team.getId(), Plan.FREE, PlanStatus.ACTIVE, null, null));
        var retro = retroRepository.saveAndFlush(new RetroEntity(team.getId(), "valid-1-column.yml"));
        var completedActionItem = actionItemRepository.saveAndFlush(
                new ActionItemEntity(null, "Do the thing", true, false, team.getId(), "me", Instant.now()));
        entityManager.flush();
        entityManager.clear();

        retroService.setFinished(retro.getId(), true);

        entityManager.flush();
        entityManager.clear();
        assertThat(actionItemRepository.findById(completedActionItem.getId()).orElseThrow().isArchived())
                .as("action items must be archived before the retro is deleted")
                .isTrue();
        assertThat(retroRepository.findById(retro.getId()))
                .as("retro should be deleted for a team without HISTORICAL_RETROS")
                .isEmpty();
    }
}
