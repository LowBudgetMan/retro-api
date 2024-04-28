package io.nickreuter.retroapi.team.usermapping;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}