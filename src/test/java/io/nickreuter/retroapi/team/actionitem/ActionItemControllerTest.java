package io.nickreuter.retroapi.team.actionitem;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ActionItemControllerTest {
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private UserMappingAuthorizationService userMappingAuthorizationService;
    @MockBean
    private ActionItemService actionItemService;
    @MockBean
    private ActionItemAuthorizationService actionItemAuthorizationService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createActionItem_Returns201() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        when(actionItemService.createActionItem("action", "assignee", teamId)).thenReturn(new ActionItemEntity(actionItemId, "action", false, teamId, "assignee", Instant.now()));
        mockMvc.perform(post("/api/teams/%s/action-items".formatted(teamId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateActionItemRequest("action", "assignee", teamId))))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/teams/%s/action-items/%s".formatted(teamId, actionItemId)));
    }

    @Test
    void createActionItem_WhenBadTokenUsed_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        mockMvc.perform(post("/api/teams/%s/action-items".formatted(teamId))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateActionItemRequest("action", "assignee", teamId))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createActionItem_WhenUserNotMemberOfTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(post("/api/teams/%s/action-items".formatted(teamId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateActionItemRequest("action", "assignee", teamId))))
                .andExpect(status().isForbidden());
    }

    @Test
    void setAction_Returns204() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = UUID.randomUUID();
        when(actionItemAuthorizationService.canUserModifyActionItem(createAuthentication(), actionItemId)).thenReturn(true);
        mockMvc.perform(put("/api/teams/%s/action-items/%s/action".formatted(teamId, actionItemId))
                    .with(jwt())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new UpdateActionItemActionRequest("new action"))))
                .andExpect(status().isNoContent());
        verify(actionItemService).setAction(actionItemId, "new action");
    }

    @Test
    void setAction_WithBadToken_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = UUID.randomUUID();
        mockMvc.perform(put("/api/teams/%s/action-items/%s/action".formatted(teamId, actionItemId))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateActionItemActionRequest("new action"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setAction_WhenUserNotMemberOfTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = UUID.randomUUID();
        when(actionItemAuthorizationService.canUserModifyActionItem(createAuthentication(), actionItemId)).thenReturn(false);
        mockMvc.perform(put("/api/teams/%s/action-items/%s/action".formatted(teamId, actionItemId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateActionItemActionRequest("new action"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void setAssignee_Returns204() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = UUID.randomUUID();
        when(actionItemAuthorizationService.canUserModifyActionItem(createAuthentication(), actionItemId)).thenReturn(true);
        mockMvc.perform(put("/api/teams/%s/action-items/%s/assignee".formatted(teamId, actionItemId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateActionItemAssigneeRequest("new assignee"))))
                .andExpect(status().isNoContent());
        verify(actionItemService).setAssignee(actionItemId, "new assignee");
    }

    @Test
    void setAssignee_WithBadToken_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = UUID.randomUUID();
        mockMvc.perform(put("/api/teams/%s/action-items/%s/assignee".formatted(teamId, actionItemId))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateActionItemAssigneeRequest("new assignee"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setAssignee_WhenUserNotMemberOfTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = UUID.randomUUID();
        when(actionItemAuthorizationService.canUserModifyActionItem(createAuthentication(), actionItemId)).thenReturn(false);
        mockMvc.perform(put("/api/teams/%s/action-items/%s/assignee".formatted(teamId, actionItemId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateActionItemAssigneeRequest("new assignee"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void setCompleted_Returns204() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = UUID.randomUUID();
        when(actionItemAuthorizationService.canUserModifyActionItem(createAuthentication(), actionItemId)).thenReturn(true);
        mockMvc.perform(put("/api/teams/%s/action-items/%s/completed".formatted(teamId, actionItemId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateActionItemCompletedRequest(true))))
                .andExpect(status().isNoContent());
        verify(actionItemService).setCompleted(actionItemId, true);
    }

    @Test
    void setCompleted_WithBadToken_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = UUID.randomUUID();
        mockMvc.perform(put("/api/teams/%s/action-items/%s/completed".formatted(teamId, actionItemId))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateActionItemCompletedRequest(true))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setCompleted_WhenUserNotMemberOfTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = UUID.randomUUID();
        when(actionItemAuthorizationService.canUserModifyActionItem(createAuthentication(), actionItemId)).thenReturn(false);
        mockMvc.perform(put("/api/teams/%s/action-items/%s/completed".formatted(teamId, actionItemId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateActionItemCompletedRequest(true))))
                .andExpect(status().isForbidden());
    }
}