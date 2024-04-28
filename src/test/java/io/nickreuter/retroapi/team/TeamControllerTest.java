package io.nickreuter.retroapi.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nickreuter.retroapi.team.exception.TeamAlreadyExistsException;
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
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class TeamControllerTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private TeamService service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTeam_Returns201WithLocationIncludingTeamId() throws Exception {
        var teamId = UUID.randomUUID();
        var teamName = "Team name";
        when(service.createTeam(teamName, "user")).thenReturn(new TeamEntity(teamId, teamName, Instant.now()));

        mockMvc.perform(post("/api/team")
                        .with(jwt())
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest(teamName)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/team/%s".formatted(teamId)));
    }

    @Test
    void createTeam_WithInvalidToken_Throws401() throws Exception {
        mockMvc.perform(post("/api/team")
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
        mockMvc.perform(post("/api/team")
                        .with(jwt())
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest(teamName)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}