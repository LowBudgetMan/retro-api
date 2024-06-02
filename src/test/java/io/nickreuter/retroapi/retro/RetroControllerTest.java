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
import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}