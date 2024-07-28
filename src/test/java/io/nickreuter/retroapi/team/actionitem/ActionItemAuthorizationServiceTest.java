package io.nickreuter.retroapi.team.actionitem;

import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActionItemAuthorizationServiceTest {
    private final UserMappingAuthorizationService userMappingAuthorizationService = mock(UserMappingAuthorizationService.class);
    private final ActionItemService actionItemService = mock(ActionItemService.class);
    private final ActionItemAuthorizationService subject = new ActionItemAuthorizationService(actionItemService, userMappingAuthorizationService);

    @Test
    void canUserModifyActionItem_WhenActionItemDoesNotExist_ReturnsFalse() {
        var authentication = mock(Authentication.class);
        var actionItemId = UUID.randomUUID();
        when(actionItemService.getActionItem(actionItemId)).thenReturn(Optional.empty());

        assertThat(subject.canUserModifyActionItem(authentication, actionItemId)).isFalse();
    }

    @Test
    void canUserModifyActionItem_WhenUserNotPartOfActionItemTeam_ReturnsFalse() {
        var authentication = mock(Authentication.class);
        var actionItemId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var actionItem = new ActionItemEntity(actionItemId, "action", false, teamId, "assignee", Instant.now());
        when(actionItemService.getActionItem(actionItemId)).thenReturn(Optional.of(actionItem));
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);

        assertThat(subject.canUserModifyActionItem(authentication, actionItemId)).isFalse();
    }

    @Test
    void canUserModifyActionItem_WhenUserPartOfActionItemTeam_ReturnsTrue() {
        var authentication = mock(Authentication.class);
        var actionItemId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var actionItem = new ActionItemEntity(actionItemId, "action", false, teamId, "assignee", Instant.now());
        when(actionItemService.getActionItem(actionItemId)).thenReturn(Optional.of(actionItem));
        when(userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);

        assertThat(subject.canUserModifyActionItem(authentication, actionItemId)).isTrue();
    }
}