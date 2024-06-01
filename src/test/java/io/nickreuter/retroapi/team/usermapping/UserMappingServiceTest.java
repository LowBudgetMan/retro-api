package io.nickreuter.retroapi.team.usermapping;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserMappingServiceTest {

    private final UserMappingRepository userMappingRepository = mock(UserMappingRepository.class);
    private final UserMappingService service = new UserMappingService(userMappingRepository);

    @Test
    void addUserToTeam_SavesRecordToRepositoryWithTeamAndUserId() {
        var teamId = UUID.randomUUID();
        var userId = "User ID";
        service.addUserToTeam(userId, teamId);
        verify(userMappingRepository).save(ArgumentMatchers.argThat((UserMappingEntity entity) ->
                entity.getId() == null &&
                entity.getTeamId() == teamId &&
                Objects.equals(entity.getUserId(), userId) &&
                entity.getCreatedAt() == null)
        );
    }

    @Test
    void getTeamsForUser_ReturnsSetFromRepository() {
        var expected = Set.of(
                new UserMappingEntity(UUID.randomUUID(), UUID.randomUUID(), "userId", Instant.now()),
                new UserMappingEntity(UUID.randomUUID(), UUID.randomUUID(), "userId", Instant.now())
        );
        when(userMappingRepository.findAllByUserId("userId")).thenReturn(expected);
        var actual = service.getTeamsForUser("userId");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void removeUserFromTeam_CallsRemoveOnRepository() {
        var teamId = UUID.randomUUID();
        var userId = "user ID";
        service.removeUserFromTeam(teamId, userId);
        verify(userMappingRepository).deleteAllByTeamIdAndUserId(teamId, userId);
    }
}