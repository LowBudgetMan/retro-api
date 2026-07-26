package io.nickreuter.retroapi.configuration;

import io.nickreuter.retroapi.team.subscription.Plan;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "retro.teams")
public record RetroTeamProperties(
        @NotNull(message = "Property 'retro.team.default-plan' is mandatory")
        Plan defaultPlan
) { }
