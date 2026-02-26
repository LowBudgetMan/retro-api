package io.nickreuter.retroapi.share;

import io.nickreuter.retroapi.retro.Retro;
import io.nickreuter.retroapi.retro.RetroAuthorizationService;
import io.nickreuter.retroapi.retro.RetroService;
import io.nickreuter.retroapi.retro.anonymousparticipant.ShareToken;
import io.nickreuter.retroapi.retro.anonymousparticipant.ShareTokenService;
import io.nickreuter.retroapi.retro.template.Template;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ShareLinkControllerTest {
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private UserMappingAuthorizationService userMappingAuthorizationService;
    @MockitoBean
    private RetroAuthorizationService retroAuthorizationService;
    @MockitoBean
    private ShareTokenService shareTokenService;
    @MockitoBean
    private RetroService retroService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validateShareLink_WithValidToken_ReturnsTeamIdAndRetroId() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var token = "valid-token";
        var shareToken = new ShareToken(UUID.randomUUID(), token, retroId);
        var template = new Template("t1", "Test", "desc", List.of());
        var retro = new Retro(retroId, teamId, false, template, Set.of(), Instant.now());

        when(shareTokenService.getShareToken(token)).thenReturn(Optional.of(shareToken));
        when(retroService.getRetro(retroId)).thenReturn(Optional.of(retro));

        mockMvc.perform(get("/api/share/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.retroId").value(retroId.toString()));
    }

    @Test
    void validateShareLink_WithInvalidToken_Returns404() throws Exception {
        when(shareTokenService.getShareToken("invalid-token")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/share/{token}", "invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validateShareLink_WhenRetroNotFound_Returns404() throws Exception {
        var retroId = UUID.randomUUID();
        var token = "valid-token";
        var shareToken = new ShareToken(UUID.randomUUID(), token, retroId);

        when(shareTokenService.getShareToken(token)).thenReturn(Optional.of(shareToken));
        when(retroService.getRetro(retroId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/share/{token}", token))
                .andExpect(status().isNotFound());
    }
}
