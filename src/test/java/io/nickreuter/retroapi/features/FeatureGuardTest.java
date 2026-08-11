package io.nickreuter.retroapi.features;

import io.nickreuter.retroapi.team.subscription.Feature;
import io.nickreuter.retroapi.team.subscription.FeatureGuard;
import io.nickreuter.retroapi.team.subscription.Plan;
import io.nickreuter.retroapi.team.subscription.TeamSubscriptionService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeatureGuardTest {
    private final TeamSubscriptionService mockSubscriptionService = mock(TeamSubscriptionService.class);
    private final FeatureGuard subject = new FeatureGuard(mockSubscriptionService);

    @Test
    void hasFeature_WhenEffectivePlanGrantsFeature_ReturnsTrue() {
        var teamId = UUID.randomUUID();
        when(mockSubscriptionService.getEffectivePlanForTeam(teamId)).thenReturn(Plan.PRO);

        assertThat(subject.hasFeature(teamId, Feature.HISTORICAL_RETROS)).isTrue();
    }

    @Test
    void hasFeature_WhenEffectivePlanDoesNotGrantFeature_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        when(mockSubscriptionService.getEffectivePlanForTeam(teamId)).thenReturn(Plan.FREE);

        assertThat(subject.hasFeature(teamId, Feature.HISTORICAL_RETROS)).isFalse();
    }

    @Test
    void hasFeature_WhenFeatureIsOnlyOnEnterprise_ReturnsFalseForPro() {
        var teamId = UUID.randomUUID();
        when(mockSubscriptionService.getEffectivePlanForTeam(teamId)).thenReturn(Plan.PRO);

        assertThat(subject.hasFeature(teamId, Feature.CUSTOM_TEMPLATES)).isFalse();
    }
}
