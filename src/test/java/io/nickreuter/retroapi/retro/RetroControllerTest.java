package io.nickreuter.retroapi.retro;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nickreuter.retroapi.retro.template.Template;
import io.nickreuter.retroapi.retro.thought.ThoughtEntity;
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
import java.util.Set;
import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    @MockBean
    private RetroAuthorizationService retroAuthorizationService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private final Template savedTemplate = new Template("template-id", "template-name", "description", List.of());

    @Test
    void createRetro_Returns201WithLocation() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(retroService.createRetro(teamId, "template-id")).thenReturn(new RetroEntity(retroId, teamId, false, "template-id", Set.of(), Instant.now()));
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        mockMvc.perform(post("/api/teams/%s/retros".formatted(teamId))
                    .with(jwt())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new CreateRetroRequest("template-id"))))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/teams/%s/retros/%s".formatted(teamId, retroId)));
    }

    @Test
    void createRetro_WhenTemplateIdDoesNotExist_Returns400() throws Exception {
        var teamId = UUID.randomUUID();
        when(retroService.createRetro(teamId, "oops")).thenThrow(InvalidTemplateIdException.class);
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        mockMvc.perform(post("/api/teams/%s/retros".formatted(teamId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRetroRequest("oops"))))
                .andExpect(status().isBadRequest());
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRetroRequest("id"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRetros_ReturnsRetros() throws Exception {
        var teamId = UUID.randomUUID();
        var retro1 = new RetroEntity(UUID.randomUUID(), teamId, false, "template-id", Set.of(), Instant.now());
        var retro2 = new RetroEntity(UUID.randomUUID(), teamId, false, "template-id", Set.of(), Instant.now());
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        when(retroService.getRetros(teamId)).thenReturn(List.of(retro1, retro2));
        mockMvc.perform(get("/api/teams/%s/retros".formatted(teamId))
                    .with(jwt())
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(retro1.getId().toString()))
                .andExpect(jsonPath("$.[0].teamId").value(retro1.getTeamId().toString()))
                .andExpect(jsonPath("$.[0].finished").value(retro1.isFinished()))
                .andExpect(jsonPath("$.[0].createdAt").value(retro1.getCreatedAt().toString()))
                .andExpect(jsonPath("$.[1].id").value(retro2.getId().toString()))
                .andExpect(jsonPath("$.[1].teamId").value(retro2.getTeamId().toString()))
                .andExpect(jsonPath("$.[1].finished").value(retro2.isFinished()))
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

    @Test
    void getRetro_ReturnsRetro() throws Exception {
        var teamId = UUID.randomUUID();
        var retro = new Retro(UUID.randomUUID(), teamId, false, savedTemplate, Set.of(), Instant.now());
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), teamId, retro.id())).thenReturn(true);
        when(retroService.getRetro(retro.id())).thenReturn(Optional.of(retro));
        mockMvc.perform(get("/api/teams/%s/retros/%s".formatted(teamId, retro.id()))
                    .with(jwt())
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(retro.id().toString()))
                .andExpect(jsonPath("$.teamId").value(retro.teamId().toString()))
                .andExpect(jsonPath("$.createdAt").value(retro.createdAt().toString()));
    }

    @Test
    void getRetro_WhenRetroHasThoughts_ReturnsThoughts() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thought1 = new ThoughtEntity(UUID.randomUUID(), "message 1", 2, true, "category 1", retroId, Instant.now());
        var thought2 = new ThoughtEntity(UUID.randomUUID(), "message 2", 0, false, "category 2", retroId, Instant.now());
        var retro = new Retro(retroId, teamId, false, savedTemplate, Set.of(thought1, thought2), Instant.now());
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), teamId, retro.id())).thenReturn(true);
        when(retroService.getRetro(retro.id())).thenReturn(Optional.of(retro));
        mockMvc.perform(get("/api/teams/%s/retros/%s".formatted(teamId, retro.id()))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thoughts.[*].id", containsInAnyOrder(thought1.getId().toString(), thought2.getId().toString())))
                .andExpect(jsonPath("$.thoughts.[*].message", containsInAnyOrder(thought1.getMessage(), thought2.getMessage())))
                .andExpect(jsonPath("$.thoughts.[*].votes", containsInAnyOrder(thought1.getVotes(), thought2.getVotes())))
                .andExpect(jsonPath("$.thoughts.[*].completed", containsInAnyOrder(thought1.isCompleted(), thought2.isCompleted())))
                .andExpect(jsonPath("$.thoughts.[*].category", containsInAnyOrder(thought1.getCategory(), thought2.getCategory())))
                .andExpect(jsonPath("$.thoughts.[*].retroId", containsInAnyOrder(thought1.getRetroId().toString(), thought2.getRetroId().toString())))
                .andExpect(jsonPath("$.thoughts.[*].createdAt", containsInAnyOrder(thought1.getCreatedAt().toString(), thought2.getCreatedAt().toString())));
    }

    @Test
    void getRetro_WhenUserNotAllowedInRetro_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var retro = new RetroEntity(UUID.randomUUID(), teamId, false, savedTemplate.id(), Set.of(), Instant.now());
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), teamId, retro.getId())).thenReturn(false);
        mockMvc.perform(get("/api/teams/%s/retros/%s".formatted(teamId, retro.getId()))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRetro_WhenBadToken_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var retro = new RetroEntity(UUID.randomUUID(), teamId, false, savedTemplate.id(), Set.of(), Instant.now());
        mockMvc.perform(get("/api/teams/%s/retros/%s".formatted(teamId, retro.getId()))
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateFinished_Returns204() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), teamId, retroId)).thenReturn(true);
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        mockMvc.perform(put("/api/teams/%s/retros/%s/finished".formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRetroFinishedRequest(true))))
                .andExpect(status().isNoContent());

        verify(retroService).setFinished(retroId, true);
    }

    @Test
    void updateFinished_WhenBadToken_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        mockMvc.perform(put("/api/teams/%s/retros/%s/finished".formatted(teamId, retroId))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRetroFinishedRequest(true))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateFinished_WhenUserNotAllowedInRetro_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), teamId, retroId)).thenReturn(false);
        mockMvc.perform(put("/api/teams/%s/retros/%s/finished".formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRetroFinishedRequest(true))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateFinished_WhenRetroNotFound_ReturnsNotFound() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), teamId, retroId)).thenReturn(true);
        doThrow(RetroNotFoundException.class).when(retroService).setFinished(retroId, true);
        mockMvc.perform(put("/api/teams/%s/retros/%s/finished".formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRetroFinishedRequest(true))))
                .andExpect(status().isNotFound());

        verify(retroService).setFinished(retroId, true);
    }
}