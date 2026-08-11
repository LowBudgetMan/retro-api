package io.nickreuter.retroapi.team.subscription;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FeatureGuard {
    private final TeamSubscriptionService teamSubscriptionService;

    public FeatureGuard(TeamSubscriptionService teamSubscriptionService) {
        this.teamSubscriptionService = teamSubscriptionService;
    }

    public boolean hasFeature(UUID teamId, Feature feature) {
        return teamSubscriptionService.getEffectivePlanForTeam(teamId).grants(feature);
    }
}
