package io.nickreuter.retroapi.team.apitoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class ApiTokenControllerTest {
    @MockitoBean private JwtDecoder jwtDecoder;
    @MockitoBean private UserMappingAuthorizationService userMappingAuthorizationService;
    @MockitoBean private ApiTokenService apiTokenService;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void getTokens_WhenUserIsTeamMember_ReturnsList() throws Exception {
        var teamId = UUID.randomUUID();
        var entity = new ApiTokenEntity(UUID.randomUUID(), teamId, "Slack", "h", "retro_pat_abcd", "read", Instant.now(), "u", null, null);
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        when(apiTokenService.getTokensForTeam(teamId)).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/teams/%s/api-tokens".formatted(teamId)).with(jwt()).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(entity.getId().toString()))
            .andExpect(jsonPath("$[0].name").value("Slack"))
            .andExpect(jsonPath("$[0].tokenPrefix").value("retro_pat_abcd"))
            .andExpect(jsonPath("$[0].scopes[0]").value("read"));
    }

    @Test
    void getTokens_WhenUserNotTeamMember_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);

        mockMvc.perform(get("/api/teams/%s/api-tokens".formatted(teamId)).with(jwt()).with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void createToken_ReturnsTokenOnce() throws Exception {
        var teamId = UUID.randomUUID();
        var entity = new ApiTokenEntity(UUID.randomUUID(), teamId, "Slack", "h", "retro_pat_abcd", "read", Instant.now(), "u", null, null);
        var created = new ApiTokenService.CreatedToken(entity, "retro_pat_fullsecretvalue");
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        when(apiTokenService.createToken(eq(teamId), eq("Slack"), eq(Set.of("read")), eq(null), any())).thenReturn(created);

        mockMvc.perform(post("/api/teams/%s/api-tokens".formatted(teamId))
                .with(jwt()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateApiTokenRequest("Slack", Set.of("read"), null))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").value("retro_pat_fullsecretvalue"))
            .andExpect(jsonPath("$.tokenPrefix").value("retro_pat_abcd"))
            .andExpect(jsonPath("$.name").value("Slack"));
    }

    @Test
    void createToken_WhenUserNotTeamMember_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);

        mockMvc.perform(post("/api/teams/%s/api-tokens".formatted(teamId))
                .with(jwt()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateApiTokenRequest("X", Set.of("read"), null))))
            .andExpect(status().isForbidden());
    }

    @Test
    void deleteToken_DelegatesToService() throws Exception {
        var teamId = UUID.randomUUID();
        var tokenId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);

        mockMvc.perform(delete("/api/teams/%s/api-tokens/%s".formatted(teamId, tokenId))
                .with(jwt()).with(csrf()))
            .andExpect(status().isNoContent());

        verify(apiTokenService).deleteToken(tokenId);
    }
}
