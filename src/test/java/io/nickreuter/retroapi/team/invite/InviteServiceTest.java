package io.nickreuter.retroapi.team.invite;

import io.nickreuter.retroapi.team.TeamEntity;
import io.nickreuter.retroapi.team.TeamService;
import io.nickreuter.retroapi.team.exception.TeamNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InviteServiceTest {
    private final TeamService teamService = mock(TeamService.class);
    private final InviteRepository inviteRepository = mock(InviteRepository.class);
    private final InviteService subject = new InviteService(teamService, inviteRepository);

    @Test
    void createInvite_WhenTeamExists_ShouldCreateInvite() throws Exception {
        var teamId = UUID.randomUUID();
        var expected = new InviteEntity(UUID.randomUUID(), teamId, Instant.now());
        when(teamService.getTeam(teamId)).thenReturn(Optional.of(new TeamEntity(teamId, "Team 1", Instant.now())));
        when(inviteRepository.save(ArgumentMatchers.argThat((var invite) ->
                invite.getId() == null &&
                invite.getTeamId().equals(teamId) &&
                invite.getCreatedAt() == null))
        ).thenReturn(expected);

        var actual = subject.createInvite(teamId);

        assertEquals(expected, actual);
    }

    @Test
    void createInvite_WhenTeamDoesNotExist_ShouldThrowTeamNotFoundException() {
        var teamId = UUID.randomUUID();
        when(teamService.getTeam(teamId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subject.createInvite(teamId)).isInstanceOf(TeamNotFoundException.class);
    }
}