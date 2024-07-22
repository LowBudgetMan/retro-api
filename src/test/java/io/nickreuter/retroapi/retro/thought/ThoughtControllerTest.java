package io.nickreuter.retroapi.retro.thought;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nickreuter.retroapi.retro.RetroAuthorizationService;
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
import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ThoughtControllerTest {
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private RetroAuthorizationService retroAuthorizationService;
    @MockBean
    private ThoughtAuthorizationService thoughtAuthorizationService;
    @MockBean
    private ThoughtService thoughtService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createThought_WhenValidRequest_Returns201() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var request = new CreateThoughtRequest("message", "category");
        var expected = ThoughtEntity.from(request.message(), request.category(), retroId);
        expected.setId(UUID.randomUUID());
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), retroId)).thenReturn(true);
        when(thoughtService.createThought(retroId, request.message(), request.category())).thenReturn(expected);

        mockMvc.perform(post("/api/teams/%s/retros/%s/thoughts".formatted(teamId, retroId))
                    .with(jwt())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/teams/%s/retros/%s/thoughts/%s".formatted(teamId, retroId, expected.getId())));
    }

    @Test
    void createThought_WhenTokenInvalid_Returns401() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var request = new CreateThoughtRequest("message", "category");

        mockMvc.perform(post("/api/teams/%s/retros/%s/thoughts".formatted(teamId, retroId))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createThought_WhenUserNotMemberOfTeam_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var request = new CreateThoughtRequest("message", "category");
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), retroId)).thenReturn(false);

        mockMvc.perform(post("/api/teams/%s/retros/%s/thoughts".formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getThoughts_Returns200WithListFromService() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var expectedThought = new ThoughtEntity(UUID.randomUUID(), "message", 0, false, "category", retroId, Instant.now());
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), retroId)).thenReturn(true);
        when(thoughtService.getThoughtsForRetro(retroId)).thenReturn(List.of(expectedThought));

        mockMvc.perform(get("/api/teams/%s/retros/%s/thoughts".formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(expectedThought.getId().toString()))
                .andExpect(jsonPath("$.[0].message").value(expectedThought.getMessage()))
                .andExpect(jsonPath("$.[0].votes").value(expectedThought.getVotes()))
                .andExpect(jsonPath("$.[0].completed").value(expectedThought.isCompleted()))
                .andExpect(jsonPath("$.[0].category").value(expectedThought.getCategory()))
                .andExpect(jsonPath("$.[0].retroId").value(expectedThought.getRetroId().toString()))
                .andExpect(jsonPath("$.[0].createdAt").value(expectedThought.getCreatedAt().toString()));
    }

    @Test
    void getThoughts_WhenInvalidToken_Returns401() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();

        mockMvc.perform(get("/api/teams/%s/retros/%s/thoughts".formatted(teamId, retroId))
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getThoughts_WhenUserNotOnTeam_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), retroId)).thenReturn(false);

        mockMvc.perform(get("/api/teams/%s/retros/%s/thoughts".formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void vote_Returns204WhenSuccessful() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        when(thoughtAuthorizationService.canUserModifyThought(createAuthentication(), thoughtId)).thenReturn(true);
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/votes".formatted(teamId, retroId, thoughtId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isNoContent());
        verify(thoughtService).addVote(thoughtId);
    }

    @Test
    void vote_WhenNoTokenProvided_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/votes".formatted(teamId, retroId, thoughtId))
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void vote_WhenUserNotAllowedInRetro_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        when(thoughtAuthorizationService.canUserModifyThought(createAuthentication(), thoughtId)).thenReturn(false);
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/votes".formatted(teamId, retroId, thoughtId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void setCompleted_WhenSuccessful_Returns204() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        when(thoughtAuthorizationService.canUserModifyThought(createAuthentication(), thoughtId)).thenReturn(true);
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/completed".formatted(teamId, retroId, thoughtId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateThoughtCompletionRequest(true))))
                .andExpect(status().isNoContent());
        verify(thoughtService).setCompleted(thoughtId, true);
    }

    @Test
    void setCompleted_WhenNoTokenProvided_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/completed".formatted(teamId, retroId, thoughtId))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateThoughtCompletionRequest(true))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setCompleted_WhenUserNotAllowedInRetro_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        when(thoughtAuthorizationService.canUserModifyThought(createAuthentication(), thoughtId)).thenReturn(false);
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/completed".formatted(teamId, retroId, thoughtId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateThoughtCompletionRequest(true))))
                .andExpect(status().isForbidden());
    }

    @Test
    void setCategory_WhenSuccessful_Returns204() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        when(thoughtAuthorizationService.canUserModifyThought(createAuthentication(), thoughtId)).thenReturn(true);
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/category".formatted(teamId, retroId, thoughtId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateThoughtCategoryRequest("category 2"))))
                .andExpect(status().isNoContent());
        verify(thoughtService).setCategory(thoughtId, "category 2");
    }

    @Test
    void setCategory_WhenBadToken_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/category".formatted(teamId, retroId, thoughtId))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateThoughtCategoryRequest("category 2"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setCategory_WhenUserNotInRetro_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        when(thoughtAuthorizationService.canUserModifyThought(createAuthentication(), thoughtId)).thenReturn(false);
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/category".formatted(teamId, retroId, thoughtId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateThoughtCategoryRequest("category 2"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void setMessage_WhenSuccessful_Returns204() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        when(thoughtAuthorizationService.canUserModifyThought(createAuthentication(), thoughtId)).thenReturn(true);
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/message".formatted(teamId, retroId, thoughtId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateThoughtMessageRequest("message!"))))
                .andExpect(status().isNoContent());
        verify(thoughtService).setMessage(thoughtId, "message!");
    }

    @Test
    void setMessage_WhenBadToken_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/message".formatted(teamId, retroId, thoughtId))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateThoughtMessageRequest("message!"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setMessage_WhenUserNotPartOfRetro_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        when(thoughtAuthorizationService.canUserModifyThought(createAuthentication(), thoughtId)).thenReturn(false);
        mockMvc.perform(put("/api/teams/%s/retros/%s/thoughts/%s/message".formatted(teamId, retroId, thoughtId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateThoughtMessageRequest("message!"))))
                .andExpect(status().isForbidden());
    }
}