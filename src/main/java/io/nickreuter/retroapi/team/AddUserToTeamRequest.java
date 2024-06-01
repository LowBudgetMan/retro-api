package io.nickreuter.retroapi.team;

import java.util.UUID;

public record AddUserToTeamRequest (
    UUID inviteId
) {}
