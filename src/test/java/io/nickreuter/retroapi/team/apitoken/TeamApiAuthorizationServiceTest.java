package io.nickreuter.retroapi.team.apitoken;

import io.nickreuter.retroapi.team.apitoken.authentication.ApiTokenAuthentication;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TeamApiAuthorizationServiceTest {
    private final UserMappingAuthorizationService userMappingAuthorizationService = mock(UserMappingAuthorizationService.class);
    private final TeamApiAuthorizationService subject = new TeamApiAuthorizationService(userMappingAuthorizationService);

    @Test
    void canRead_WhenJwtUserIsTeamMember_ReturnsTrue() {
        var auth = mock(Authentication.class);
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(auth, teamId)).thenReturn(true);

        assertThat(subject.canRead(auth, teamId)).isTrue();
    }

    @Test
    void canRead_WhenJwtUserIsNotTeamMember_ReturnsFalse() {
        var auth = mock(Authentication.class);
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(auth, teamId)).thenReturn(false);

        assertThat(subject.canRead(auth, teamId)).isFalse();
    }

    @Test
    void canRead_WhenApiTokenForRightTeamWithReadScope_ReturnsTrue() {
        var teamId = UUID.randomUUID();
        var auth = new ApiTokenAuthentication(UUID.randomUUID(), teamId, Set.of("read"));

        assertThat(subject.canRead(auth, teamId)).isTrue();
    }

    @Test
    void canRead_WhenApiTokenForWrongTeam_ReturnsFalse() {
        var auth = new ApiTokenAuthentication(UUID.randomUUID(), UUID.randomUUID(), Set.of("read", "write"));

        assertThat(subject.canRead(auth, UUID.randomUUID())).isFalse();
    }

    @Test
    void canRead_WhenApiTokenForRightTeamButNoReadScope_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var auth = new ApiTokenAuthentication(UUID.randomUUID(), teamId, Set.of("write"));

        assertThat(subject.canRead(auth, teamId)).isFalse();
    }

    @Test
    void canWrite_WhenApiTokenForRightTeamWithWriteScope_ReturnsTrue() {
        var teamId = UUID.randomUUID();
        var auth = new ApiTokenAuthentication(UUID.randomUUID(), teamId, Set.of("write"));

        assertThat(subject.canWrite(auth, teamId)).isTrue();
    }

    @Test
    void canWrite_WhenApiTokenForRightTeamButOnlyReadScope_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var auth = new ApiTokenAuthentication(UUID.randomUUID(), teamId, Set.of("read"));

        assertThat(subject.canWrite(auth, teamId)).isFalse();
    }

    @Test
    void canWrite_WhenJwtUserIsTeamMember_ReturnsTrue() {
        var auth = mock(Authentication.class);
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(auth, teamId)).thenReturn(true);

        assertThat(subject.canWrite(auth, teamId)).isTrue();
    }
}
