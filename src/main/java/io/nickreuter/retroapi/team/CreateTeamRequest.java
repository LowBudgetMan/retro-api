package io.nickreuter.retroapi.team;

import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(@NotBlank() String name) {}
