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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}