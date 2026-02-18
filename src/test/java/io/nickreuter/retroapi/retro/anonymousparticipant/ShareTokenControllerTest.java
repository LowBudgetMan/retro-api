package io.nickreuter.retroapi.retro.anonymousparticipant;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nickreuter.retroapi.retro.CreateRetroRequest;
import io.nickreuter.retroapi.retro.RetroAuthorizationService;
import io.nickreuter.retroapi.retro.RetroService;
import io.nickreuter.retroapi.retro.UpdateRetroFinishedRequest;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ShareTokenControllerTest {
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private UserMappingAuthorizationService userMappingAuthorizationService;
    @MockitoBean
    private RetroAuthorizationService retroAuthorizationService;
    @MockitoBean
    private ShareTokenService shareTokenService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createShareToken_WithInvalidToken_Throws401() throws Exception {
        mockMvc.perform(post("/api/teams/%s/retros/%s/share-tokens".formatted(UUID.randomUUID(), UUID.randomUUID()))
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createShareToken_WhenUserIsNotMemberOfTeam_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(post("/api/teams/%s/retros/%s/share-tokens".formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createShareToken_Returns201WithTokenValueInLocation() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var expected = new ShareToken(UUID.randomUUID(), "This is a token", retroId);
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        when(shareTokenService.createShareToken(retroId)).thenReturn(expected);
        mockMvc.perform(post("/api/teams/%s/retros/%s/share-tokens".formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, expected.token()));
    }
}