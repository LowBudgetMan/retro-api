package io.nickreuter.retroapi.team.invite;

import io.nickreuter.retroapi.team.exception.TeamNotFoundException;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class InviteControllerTest {
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private UserMappingAuthorizationService userMappingAuthorizationService;
    @MockBean
    private InviteService inviteService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createInvite_Returns201CreatedWithInviteIdInLocationHeader() throws Exception {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(inviteService.createInvite(teamId)).thenReturn(new InviteEntity(inviteId, teamId, Instant.now()));
        mockMvc.perform(post("/api/teams/%s/invites".formatted(teamId.toString()))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/teams/%s/invites/%s".formatted(teamId.toString(), inviteId.toString())));
    }

    @Test
    void createInvite_WithInvalidToken_Throws401() throws Exception {
        mockMvc.perform(post("/api/teams/%s/invites".formatted(UUID.randomUUID()))
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createInvite_WhenUserNotOnTeam_Throws403() throws Exception{
        UUID teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(post("/api/teams/%s/invites".formatted(teamId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createInvite_WithInvalidTeam_Throws404() throws Exception {
        UUID teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        doThrow(TeamNotFoundException.class).when(inviteService).createInvite(teamId);
        mockMvc.perform(post("/api/teams/%s/invites".formatted(teamId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}