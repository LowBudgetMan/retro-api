package io.nickreuter.retroapi.retro;

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
import java.util.List;
import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class RetroControllerTest {
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private RetroService retroService;
    @MockBean
    private UserMappingAuthorizationService userMappingAuthorizationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createRetro_Returns201WithLocation() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(retroService.createRetro(teamId)).thenReturn(new RetroEntity(retroId, teamId, Instant.now()));
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        mockMvc.perform(post("/api/teams/%s/retros".formatted(teamId))
                    .with(jwt())
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/teams/%s/retros/%s".formatted(teamId, retroId)));
    }
    @Test
    void createRetro_WithInvalidToken_Throws401() throws Exception {
        mockMvc.perform(post("/api/teams/%s/retros".formatted(UUID.randomUUID()))
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createRetro_WhenUserIsNotMemberOfTeam_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(post("/api/teams/%s/retros".formatted(teamId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRetros_ReturnsRetros() throws Exception {
        var teamId = UUID.randomUUID();
        var retro1 = new RetroEntity(UUID.randomUUID(), teamId, Instant.now());
        var retro2 = new RetroEntity(UUID.randomUUID(), teamId, Instant.now());
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        when(retroService.getRetros(teamId)).thenReturn(List.of(retro1, retro2));
        mockMvc.perform(get("/api/teams/%s/retros".formatted(teamId))
                    .with(jwt())
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(retro1.getId().toString()))
                .andExpect(jsonPath("$.[0].teamId").value(retro1.getTeamId().toString()))
                .andExpect(jsonPath("$.[0].createdAt").value(retro1.getCreatedAt().toString()))
                .andExpect(jsonPath("$.[1].id").value(retro2.getId().toString()))
                .andExpect(jsonPath("$.[1].teamId").value(retro2.getTeamId().toString()))
                .andExpect(jsonPath("$.[1].createdAt").value(retro2.getCreatedAt().toString()));
    }

    @Test
    void getRetros_WhenBadToken_Throws401() throws Exception {
        mockMvc.perform(get("/api/teams/%s/retros".formatted(UUID.randomUUID()))
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRetros_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(get("/api/teams/%s/retros".formatted(teamId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}