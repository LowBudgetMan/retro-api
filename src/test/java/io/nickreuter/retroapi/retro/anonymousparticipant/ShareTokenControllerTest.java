package io.nickreuter.retroapi.retro.anonymousparticipant;

import io.nickreuter.retroapi.retro.RetroAuthorizationService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

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
    void createShareToken_Returns201WithTokenInBodyAndLocation() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var expected = new ShareToken(UUID.randomUUID(), "Thisisatoken", retroId);
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        when(shareTokenService.createShareToken(retroId)).thenReturn(expected);
        mockMvc.perform(post("/api/teams/%s/retros/%s/share-tokens".formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, expected.token()))
                .andExpect(jsonPath("$.token").value(expected.token()))
                .andExpect(jsonPath("$.retroId").value(retroId.toString()));
    }

    @Test
    void getShareTokens_WhenUserIsNotMemberOfTeam_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(get("/api/teams/%s/retros/%s/share-tokens".formatted(teamId, retroId))
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getShareTokens_Returns200WithTokenList() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var tokens = List.of(new ShareToken(UUID.randomUUID(), "token1", retroId));
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        when(shareTokenService.getShareTokensForRetro(retroId)).thenReturn(tokens);
        mockMvc.perform(get("/api/teams/%s/retros/%s/share-tokens".formatted(teamId, retroId))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].token").value("token1"));
    }
}