package io.nickreuter.retroapi.features;

import io.nickreuter.retroapi.configuration.RetroTeamProperties;
import io.nickreuter.retroapi.team.subscription.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.*;

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

    @Test
    void getPlanForTeam_WhenTeamExists_ReturnsPlan() {
        var teamId = UUID.randomUUID();
        var savedSubscription = new TeamSubscriptionEntity(null, teamId, Plan.ENTERPRISE, PlanStatus.ACTIVE, new Date(), new Date());
        when(mockSubscriptionRepo.findByTeamId(teamId)).thenReturn(Optional.of(savedSubscription));

        var actual = subject.getPlanForTeam(teamId);

        assertThat(actual.orElseThrow()).isEqualTo(savedSubscription.getPlan());
    }

    @Test
    void getPlanForTeam_WhenTeamDoesNotExist_ReturnsEmptyOptional() {
        var teamId = UUID.randomUUID();
        when(mockSubscriptionRepo.findByTeamId(teamId)).thenReturn(Optional.empty());

        var actual = subject.getPlanForTeam(teamId);

        assertThat(actual).isEmpty();
    }

    @Test
    void getEffectivePlanForTeam_WhenStatusIsActive_ReturnsStoredPlan() {
        var teamId = UUID.randomUUID();
        when(mockSubscriptionRepo.findByTeamId(teamId)).thenReturn(Optional.of(
                new TeamSubscriptionEntity(null, teamId, Plan.PRO, PlanStatus.ACTIVE, new Date(), new Date())));

        assertThat(subject.getEffectivePlanForTeam(teamId)).isEqualTo(Plan.PRO);
    }

    @Test
    void getEffectivePlanForTeam_WhenStatusIsTrial_ReturnsStoredPlan() {
        var teamId = UUID.randomUUID();
        when(mockSubscriptionRepo.findByTeamId(teamId)).thenReturn(Optional.of(
                new TeamSubscriptionEntity(null, teamId, Plan.ENTERPRISE, PlanStatus.TRIAL, new Date(), new Date())));

        assertThat(subject.getEffectivePlanForTeam(teamId)).isEqualTo(Plan.ENTERPRISE);
    }

    @Test
    void getEffectivePlanForTeam_WhenStatusIsCancelled_ReturnsDefaultPlan() {
        var teamId = UUID.randomUUID();
        when(mockSubscriptionRepo.findByTeamId(teamId)).thenReturn(Optional.of(
                new TeamSubscriptionEntity(null, teamId, Plan.PRO, PlanStatus.CANCELLED, new Date(), new Date())));

        assertThat(subject.getEffectivePlanForTeam(teamId)).isEqualTo(defaultPlan);
    }

    @Test
    void getEffectivePlanForTeam_WhenNoSubscriptionExists_ReturnsDefaultPlan() {
        var teamId = UUID.randomUUID();
        when(mockSubscriptionRepo.findByTeamId(teamId)).thenReturn(Optional.empty());

        assertThat(subject.getEffectivePlanForTeam(teamId)).isEqualTo(defaultPlan);
    }

    private static TeamSubscriptionEntity checkSavedEntity(TeamSubscriptionEntity expectedSubscription) {
        return assertArg(actual ->
                assertThat(actual)
                        .usingRecursiveComparison()
                        .isEqualTo(expectedSubscription)
        );
    }
}