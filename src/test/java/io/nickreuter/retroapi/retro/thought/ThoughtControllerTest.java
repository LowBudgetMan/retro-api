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

import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ThoughtControllerTest {
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private RetroAuthorizationService retroAuthorizationService;
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
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), teamId, retroId)).thenReturn(true);
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
        when(retroAuthorizationService.isUserAllowedInRetro(createAuthentication(), teamId, retroId)).thenReturn(false);

        mockMvc.perform(post("/api/teams/%s/retros/%s/thoughts".formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}