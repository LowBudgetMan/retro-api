package io.nickreuter.retroapi.team.usermapping;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserMappingAuthorizationServiceTest {
    private final UserMappingRepository userMappingRepository = mock(UserMappingRepository.class);
    private final UserMappingAuthorizationService subject = new UserMappingAuthorizationService(userMappingRepository);

    @Test
    void isUserMemberOfTeam_WhenUserIsOnTeam_ReturnsTrue() {
        var userId = "User ID";
        var teamId = UUID.randomUUID();
        when(userMappingRepository.findByTeamIdAndUserId(teamId, userId)).thenReturn(Optional.of(new UserMappingEntity(
                UUID.randomUUID(),
                teamId,
                userId,
                Instant.now())));
        assertThat(subject.isUserMemberOfTeam(new JwtAuthenticationToken(mock(Jwt.class), null, userId), teamId)).isTrue();
    }

    @Test
    void isUserMemberOfTeam_WhenUserNotOnTeam_ReturnsFalse() {
        var userId = "User ID";
        var teamId = UUID.randomUUID();
        when(userMappingRepository.findByTeamIdAndUserId(teamId, userId)).thenReturn(Optional.empty());
        assertThat(subject.isUserMemberOfTeam(new JwtAuthenticationToken(mock(Jwt.class), null, userId), teamId)).isFalse();
    }
}