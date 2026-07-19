package io.nickreuter.retroapi.features;

import io.nickreuter.retroapi.configuration.RetroTeamProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TeamSubscriptionServiceTest {
    private final Plan defaultPlan = Plan.FREE;
    private final TeamSubscriptionRepository mockSubscriptionRepo = mock(TeamSubscriptionRepository.class);
    private final TeamSubscriptionService subject = new TeamSubscriptionService(mockSubscriptionRepo, new RetroTeamProperties(defaultPlan));

    @Test
    void createSubscription_WhenPlanProvided_ShouldSaveWithPlan() {
        var teamId = UUID.randomUUID();
        var expectedPlan = Plan.PRO;
        var expectedSubscription = new TeamSubscriptionEntity(null, teamId, expectedPlan, PlanStatus.ACTIVE, null, null);

        subject.createSubscriptionForTeam(teamId, expectedPlan);

        verify(mockSubscriptionRepo).save(checkSavedEntity(expectedSubscription));
    }

    @Test
    void createSubscription_WhenPlanProvided_ShouldDefaultStatusToActive() {
        var teamId = UUID.randomUUID();
        var expectedPlan = Plan.PRO;

        subject.createSubscriptionForTeam(teamId, expectedPlan);

        ArgumentCaptor<TeamSubscriptionEntity> captor = ArgumentCaptor.forClass(TeamSubscriptionEntity.class);
        verify(mockSubscriptionRepo).save(captor.capture());
        assertThat(captor.getValue().getPlanStatus()).isEqualTo(PlanStatus.ACTIVE);
    }

    @Test
    void createSubscription_WhenNoPlanProvided_ShouldSaveWithDefaultPlan() {
        var teamId = UUID.randomUUID();
        var expectedSubscription = new TeamSubscriptionEntity(null, teamId, defaultPlan, PlanStatus.ACTIVE, null, null);

        subject.createSubscriptionForTeam(teamId);

        verify(mockSubscriptionRepo).save(checkSavedEntity(expectedSubscription));
    }

    @Test
    void createSubscription_WhenNoPlanProvided_ShouldDefaultStatusToActive() {
        var teamId = UUID.randomUUID();

        subject.createSubscriptionForTeam(teamId);

        ArgumentCaptor<TeamSubscriptionEntity> captor = ArgumentCaptor.forClass(TeamSubscriptionEntity.class);
        verify(mockSubscriptionRepo).save(captor.capture());
        assertThat(captor.getValue().getPlanStatus()).isEqualTo(PlanStatus.ACTIVE);
    }

    private static TeamSubscriptionEntity checkSavedEntity(TeamSubscriptionEntity expectedSubscription) {
        return assertArg(actual ->
                assertThat(actual)
                        .usingRecursiveComparison()
                        .isEqualTo(expectedSubscription)
        );
    }
}