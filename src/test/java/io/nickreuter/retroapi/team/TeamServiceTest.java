package io.nickreuter.retroapi.team;

import io.nickreuter.retroapi.team.exception.TeamAlreadyExistsException;
import io.nickreuter.retroapi.team.usermapping.UserMappingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class TeamServiceTest {
    private final TeamRepository teamRepository = mock(TeamRepository.class);
    private final UserMappingService userMappingService = mock(UserMappingService.class);
    private final TeamService service = new TeamService(teamRepository, userMappingService);

    @Test
    void createTeam_ShouldReturnCreatedTeam() throws TeamAlreadyExistsException {
        var expected = new TeamEntity(UUID.randomUUID(), "expected name", Instant.now());
        when(teamRepository.existsByName("expected name")).thenReturn(false);
        when(teamRepository.save(ArgumentMatchers.argThat((TeamEntity team) ->
                team.getId() == null &&
                Objects.equals(team.getName(), "expected name") &&
                team.getCreatedAt() == null))
        ).thenReturn(expected);
        var actual = service.createTeam("expected name", "User ID");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createTeam_WhenTeamAlreadyExists_ShouldThrowException() {
        when(teamRepository.existsByName("team already exists")).thenReturn(true);
        assertThatExceptionOfType(TeamAlreadyExistsException.class).isThrownBy(() -> service.createTeam("team already exists", "user ID"));
        verify(teamRepository, times(0)).save(any());
    }

    @Test
    void createTeam_ShouldAddCreatingUserToTeam() throws TeamAlreadyExistsException {
        var expected = new TeamEntity(UUID.randomUUID(), "expected team name", Instant.now());
        when(teamRepository.existsByName("expected team name")).thenReturn(false);
        when(teamRepository.save(any())).thenReturn(expected);
        var actual = service.createTeam("expected team name", "User ID");
        assertThat(actual).isEqualTo(expected);
        verify(userMappingService).addUserToTeam("User ID", actual.getId());
    }
}