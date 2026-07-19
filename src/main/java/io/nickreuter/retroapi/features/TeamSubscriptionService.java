package io.nickreuter.retroapi.features;

import io.nickreuter.retroapi.configuration.RetroTeamProperties;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TeamSubscriptionService {
    private final TeamSubscriptionRepository teamSubscriptionRepository;
    private final Plan defaultPlan;

    public TeamSubscriptionService(TeamSubscriptionRepository teamSubscriptionRepository, RetroTeamProperties teamProperties) {
        this.teamSubscriptionRepository = teamSubscriptionRepository;
        this.defaultPlan = teamProperties.defaultPlan();
    }

    public void createSubscriptionForTeam(UUID teamId) {
        teamSubscriptionRepository.save(new TeamSubscriptionEntity(null, teamId, defaultPlan, PlanStatus.ACTIVE, null, null));
    }

    public void createSubscriptionForTeam(UUID teamId, Plan plan) {
        teamSubscriptionRepository.save(new TeamSubscriptionEntity(null, teamId, plan, PlanStatus.ACTIVE, null, null));
    }
}
