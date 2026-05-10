package io.nickreuter.retroapi.team.webhook;

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
class WebhookControllerTest {
    @MockitoBean private JwtDecoder jwtDecoder;
    @MockitoBean private UserMappingAuthorizationService userMappingAuthorizationService;
    @MockitoBean private WebhookService webhookService;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void getWebhooks_WhenUserIsTeamMember_ReturnsList() throws Exception {
        var teamId = UUID.randomUUID();
        var entity = new WebhookEntity(UUID.randomUUID(), teamId, "Slack", "https://hooks.slack.com", "secret", "action_item.created", true, 0, null, null, null, Instant.now(), "user1");
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        when(webhookService.getWebhooksForTeam(teamId)).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/teams/%s/webhooks".formatted(teamId)).with(jwt()).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(entity.getId().toString()))
            .andExpect(jsonPath("$[0].name").value("Slack"))
            .andExpect(jsonPath("$[0].url").value("https://hooks.slack.com"))
            .andExpect(jsonPath("$[0].enabled").value(true));
    }

    @Test
    void getWebhooks_WhenUserNotTeamMember_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);

        mockMvc.perform(get("/api/teams/%s/webhooks".formatted(teamId)).with(jwt()).with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void createWebhook_ReturnsSecretOnce() throws Exception {
        var teamId = UUID.randomUUID();
        var entity = new WebhookEntity(UUID.randomUUID(), teamId, "Slack", "https://hooks.slack.com", "secret", "action_item.created", true, 0, null, null, null, Instant.now(), "user1");
        var created = new WebhookService.CreatedWebhook(entity, "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890");
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        when(webhookService.createWebhook(eq(teamId), eq("Slack"), eq("https://hooks.slack.com"), eq(Set.of("action_item.created")), any())).thenReturn(created);

        mockMvc.perform(post("/api/teams/%s/webhooks".formatted(teamId))
                .with(jwt()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateWebhookRequest("Slack", "https://hooks.slack.com", Set.of("action_item.created")))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.secret").value("abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"))
            .andExpect(jsonPath("$.name").value("Slack"));
    }

    @Test
    void createWebhook_WhenUserNotTeamMember_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);

        mockMvc.perform(post("/api/teams/%s/webhooks".formatted(teamId))
                .with(jwt()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateWebhookRequest("X", "https://example.com", Set.of("action_item.created")))))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateWebhook_ReturnsNoContent() throws Exception {
        var teamId = UUID.randomUUID();
        var webhookId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);

        mockMvc.perform(put("/api/teams/%s/webhooks/%s".formatted(teamId, webhookId))
                .with(jwt()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateWebhookRequest("New Name", null, null, true))))
            .andExpect(status().isNoContent());

        verify(webhookService).updateWebhook(webhookId, "New Name", null, null, true);
    }

    @Test
    void deleteWebhook_DelegatesToService() throws Exception {
        var teamId = UUID.randomUUID();
        var webhookId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);

        mockMvc.perform(delete("/api/teams/%s/webhooks/%s".formatted(teamId, webhookId))
                .with(jwt()).with(csrf()))
            .andExpect(status().isNoContent());

        verify(webhookService).deleteWebhook(webhookId);
    }
}
