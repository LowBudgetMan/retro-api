package io.nickreuter.retroapi.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nickreuter.retroapi.team.exception.TeamAlreadyExistsException;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class TeamControllerTest {
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private TeamService service;
    @MockBean
    private UserMappingAuthorizationService userMappingAuthorizationService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTeam_Returns201WithLocationIncludingTeamId() throws Exception {
        var teamId = UUID.randomUUID();
        var teamName = "Team name";
        when(service.createTeam(teamName, "user")).thenReturn(new TeamEntity(teamId, teamName, Instant.now()));

        mockMvc.perform(post("/api/teams")
                        .with(jwt())
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest(teamName)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/teams/%s".formatted(teamId)));
    }

    @Test
    void createTeam_WithInvalidToken_Throws401() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .with(anonymous())
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest("Team name")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTeam_WhenTeamAlreadyExists_Throws409() throws Exception {
        var teamName = "Team name";
        doThrow(TeamAlreadyExistsException.class).when(service).createTeam(teamName, "user");
        mockMvc.perform(post("/api/teams")
                        .with(jwt())
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest(teamName)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void getTeamsForUser_ReturnsListOfTeams() throws Exception {
        var team1 = new TeamEntity(UUID.fromString("7c52730b-b9e8-4db8-a8fd-4fd3a9d84809"), "Team 1", Instant.ofEpochSecond(1000000001));
        var team2 = new TeamEntity(UUID.fromString("be117b4d-b57f-4263-916a-e6933d6bf6fe"), "Team 2", Instant.ofEpochSecond(1000000002));
        when(service.getTeamsForUser("user")).thenReturn(List.of(team1, team2));
        mockMvc.perform(get("/api/teams")
                .with(jwt())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0].id").value("7c52730b-b9e8-4db8-a8fd-4fd3a9d84809"))
            .andExpect(jsonPath("$.[0].name").value("Team 1"))
            .andExpect(jsonPath("$.[0].createdAt").value("2001-09-09T01:46:41Z"))
            .andExpect(jsonPath("$.[1].id").value("be117b4d-b57f-4263-916a-e6933d6bf6fe"))
            .andExpect(jsonPath("$.[1].name").value("Team 2"))
            .andExpect(jsonPath("$.[1].createdAt").value("2001-09-09T01:46:42Z"));
    }

    @Test
    void getTeamsForUser_WithInvalidToken_Throws401() throws Exception {
        mockMvc.perform(get("/api/teams")
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTeam_WhenTeamExists_ReturnsTeam() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(service.getTeam(teamId)).thenReturn(Optional.of(new TeamEntity(teamId, "Team 1", Instant.ofEpochMilli(20000000))));
        mockMvc.perform(get("/api/teams/%s".formatted(teamId))
                .with(jwt())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(teamId.toString()))
            .andExpect(jsonPath("$.name").value("Team 1"))
            .andExpect(jsonPath("$.createdAt").value(Instant.ofEpochMilli(20000000).toString()));
    }

    @Test
    void getTeam_WhenUserNotOnTeam_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(get("/api/teams/%s".formatted(teamId))
                        .with(jwt()))
                .andExpect(status().isForbidden());

    }

    @Test
    void getTeam_WhenInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/teams/%s".formatted(UUID.randomUUID()))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void getTeam_WhenTeamDoesNotExist_Returns404() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(service.getTeam(teamId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/teams/%s".formatted(teamId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}