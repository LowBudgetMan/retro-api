package io.nickreuter.retroapi.team.invite;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InviteServiceTest {
    private final InviteRepository inviteRepository = mock(InviteRepository.class);
    private final InviteService subject = new InviteService(inviteRepository);

    @Test
    void createInvite_WhenTeamExists_ShouldCreateInvite() {
        var teamId = UUID.randomUUID();
        var expected = new InviteEntity(UUID.randomUUID(), teamId, Instant.now());
        when(inviteRepository.save(ArgumentMatchers.argThat((var invite) ->
                invite.getId() == null &&
                invite.getTeamId().equals(teamId) &&
                invite.getCreatedAt() == null))
        ).thenReturn(expected);

        var actual = subject.createInvite(teamId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getInvitesForTeam_WhenTeamExists_ShouldReturnInvites() {
        var teamId = UUID.randomUUID();
        var expected = List.of(new InviteEntity(UUID.randomUUID(), teamId, Instant.now()));
        when(inviteRepository.findAllByTeamId(teamId)).thenReturn(expected);

        var actual = subject.getInvitesForTeam(teamId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getInviteForTeam_WhenInviteForTeamExists_ReturnsInvite() {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        var expected = Optional.of(new InviteEntity(inviteId, teamId, Instant.now()));
        when(inviteRepository.findByIdAndTeamId(inviteId, teamId)).thenReturn(expected);

        var actual = subject.getInviteForTeam(teamId, inviteId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getInviteForTeam_WhenInviteForTeamDoesNotExist_ReturnsEmptyOptional() {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        when(inviteRepository.findByIdAndTeamId(inviteId, teamId)).thenReturn(Optional.empty());

        var actual = subject.getInviteForTeam(teamId, inviteId);

        assertThat(actual).isEmpty();
    }

    @Test
    void deleteInvite_PassesInviteIdToRepository() {
        var inviteId = UUID.randomUUID();
        subject.deleteInvite(inviteId);
        verify(inviteRepository).deleteById(inviteId);
    }
}