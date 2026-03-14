package io.nickreuter.retroapi.retro.event;

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

import java.util.UUID;

import static io.nickreuter.retroapi.team.TestAuthenticationCreationService.createAuthentication;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class RetroEventControllerTest {
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private RetroEventService retroEventService;
    @MockitoBean
    private UserMappingAuthorizationService userMappingAuthorizationService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/teams/%s/retros/%s/events";

    // Timer Start
    @Test
    void startTimer_Returns200() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        mockMvc.perform(post((BASE_URL + "/timer-start").formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimerStartRequest(300))))
                .andExpect(status().isOk());
        verify(retroEventService).publishTimerStart(retroId, 300);
    }

    @Test
    void startTimer_WhenAnonymous_Returns401() throws Exception {
        mockMvc.perform(post((BASE_URL + "/timer-start").formatted(UUID.randomUUID(), UUID.randomUUID()))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimerStartRequest(300))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void startTimer_WhenNotMember_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(post((BASE_URL + "/timer-start").formatted(teamId, UUID.randomUUID()))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimerStartRequest(300))))
                .andExpect(status().isForbidden());
    }

    // Timer Stop
    @Test
    void stopTimer_Returns200() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        mockMvc.perform(post((BASE_URL + "/timer-stop").formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk());
        verify(retroEventService).publishTimerStop(retroId);
    }

    @Test
    void stopTimer_WhenAnonymous_Returns401() throws Exception {
        mockMvc.perform(post((BASE_URL + "/timer-stop").formatted(UUID.randomUUID(), UUID.randomUUID()))
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void stopTimer_WhenNotMember_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(post((BASE_URL + "/timer-stop").formatted(teamId, UUID.randomUUID()))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // Focus
    @Test
    void focusThought_Returns200() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        mockMvc.perform(post((BASE_URL + "/focus").formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FocusRequest(thoughtId))))
                .andExpect(status().isOk());
        verify(retroEventService).publishFocus(retroId, thoughtId);
    }

    @Test
    void focusThought_WhenAnonymous_Returns401() throws Exception {
        mockMvc.perform(post((BASE_URL + "/focus").formatted(UUID.randomUUID(), UUID.randomUUID()))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FocusRequest(UUID.randomUUID()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void focusThought_WhenNotMember_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(post((BASE_URL + "/focus").formatted(teamId, UUID.randomUUID()))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FocusRequest(UUID.randomUUID()))))
                .andExpect(status().isForbidden());
    }

    // Focus Clear
    @Test
    void clearFocus_Returns200() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        mockMvc.perform(post((BASE_URL + "/focus-clear").formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk());
        verify(retroEventService).publishFocusClear(retroId);
    }

    @Test
    void clearFocus_WhenAnonymous_Returns401() throws Exception {
        mockMvc.perform(post((BASE_URL + "/focus-clear").formatted(UUID.randomUUID(), UUID.randomUUID()))
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clearFocus_WhenNotMember_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(post((BASE_URL + "/focus-clear").formatted(teamId, UUID.randomUUID()))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // Sort
    @Test
    void sortColumn_Returns200() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        mockMvc.perform(post((BASE_URL + "/sort").formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SortRequest("votes", "desc"))))
                .andExpect(status().isOk());
        verify(retroEventService).publishSort(retroId, "votes", "desc");
    }

    @Test
    void sortColumn_WhenAnonymous_Returns401() throws Exception {
        mockMvc.perform(post((BASE_URL + "/sort").formatted(UUID.randomUUID(), UUID.randomUUID()))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SortRequest("votes", "desc"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sortColumn_WhenNotMember_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(post((BASE_URL + "/sort").formatted(teamId, UUID.randomUUID()))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SortRequest("votes", "desc"))))
                .andExpect(status().isForbidden());
    }

    // Phase
    @Test
    void changePhase_Returns200() throws Exception {
        var teamId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(true);
        mockMvc.perform(post((BASE_URL + "/phase").formatted(teamId, retroId))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PhaseRequest("voting"))))
                .andExpect(status().isOk());
        verify(retroEventService).publishPhase(retroId, "voting");
    }

    @Test
    void changePhase_WhenAnonymous_Returns401() throws Exception {
        mockMvc.perform(post((BASE_URL + "/phase").formatted(UUID.randomUUID(), UUID.randomUUID()))
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PhaseRequest("voting"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePhase_WhenNotMember_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        when(userMappingAuthorizationService.isUserMemberOfTeam(createAuthentication(), teamId)).thenReturn(false);
        mockMvc.perform(post((BASE_URL + "/phase").formatted(teamId, UUID.randomUUID()))
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PhaseRequest("voting"))))
                .andExpect(status().isForbidden());
    }
}
