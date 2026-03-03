package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.anonymousparticipant.ShareToken;
import io.nickreuter.retroapi.retro.anonymousparticipant.ShareTokenService;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetroAuthorizationServiceTest {
    private final UserMappingAuthorizationService userMappingAuthorizationService = mock(UserMappingAuthorizationService.class);
    private final RetroService retroService = mock(RetroService.class);
    private final ShareTokenService shareTokenService = mock(ShareTokenService.class);
    private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    private final Authentication authentication = mock(Authentication.class);
    private final RetroAuthorizationService retroAuthorizationService = new RetroAuthorizationService(userMappingAuthorizationService, retroService, shareTokenService, httpServletRequest);

    @Test
    void isUserAllowedInRetro_WhenRetroDoesNotExist_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(retroService.getRetro(retroId)).thenReturn(Optional.empty());

        assertThat(retroAuthorizationService.isUserAllowedInRetro(authentication, retroId)).isFalse();
    }

    @Test
    void isUserAllowedInRetro_WhenRetroTeamIdDoesNotMatchTeamIdInUrl_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(retroService.getRetro(retroId)).thenReturn(Optional.of(new Retro(retroId, UUID.randomUUID(), false, null, Set.of(), Instant.now())));

        assertThat(retroAuthorizationService.isUserAllowedInRetro(authentication, retroId)).isFalse();
    }

    @Test
    void isUserAllowedInRetro_WhenRetroTeamIdMatchesTeamIdInUrl_ReturnsTrue() {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(retroService.getRetro(retroId)).thenReturn(Optional.of(new Retro(retroId, teamId, false, null, Set.of(), Instant.now())));

        assertThat(retroAuthorizationService.isUserAllowedInRetro(authentication, retroId)).isTrue();
    }

    @Test
    void isUserAllowedInRetro_WhenJwtUserNotTeamMemberButHasValidShareTokenForRetro_ReturnsTrue() {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        when(retroService.getRetro(retroId)).thenReturn(Optional.of(new Retro(retroId, teamId, false, null, Set.of(), Instant.now())));
        when(httpServletRequest.getHeader("X-Share-Token")).thenReturn("valid-token");
        when(shareTokenService.getShareToken("valid-token")).thenReturn(Optional.of(new ShareToken(UUID.randomUUID(), "valid-token", retroId)));

        assertThat(retroAuthorizationService.isUserAllowedInRetro(authentication, retroId)).isTrue();
    }

    @Test
    void isUserAllowedInRetro_WhenJwtUserNotTeamMemberAndShareTokenIsForDifferentRetro_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var differentRetroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        when(retroService.getRetro(retroId)).thenReturn(Optional.of(new Retro(retroId, teamId, false, null, Set.of(), Instant.now())));
        when(httpServletRequest.getHeader("X-Share-Token")).thenReturn("wrong-retro-token");
        when(shareTokenService.getShareToken("wrong-retro-token")).thenReturn(Optional.of(new ShareToken(UUID.randomUUID(), "wrong-retro-token", differentRetroId)));

        assertThat(retroAuthorizationService.isUserAllowedInRetro(authentication, retroId)).isFalse();
    }

    @Test
    void isUserAllowedInRetro_WhenJwtUserNotTeamMemberAndNoShareTokenHeader_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        when(retroService.getRetro(retroId)).thenReturn(Optional.of(new Retro(retroId, teamId, false, null, Set.of(), Instant.now())));
        when(httpServletRequest.getHeader("X-Share-Token")).thenReturn(null);

        assertThat(retroAuthorizationService.isUserAllowedInRetro(authentication, retroId)).isFalse();
    }
}