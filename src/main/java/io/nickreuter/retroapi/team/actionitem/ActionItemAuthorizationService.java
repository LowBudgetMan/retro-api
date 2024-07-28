package io.nickreuter.retroapi.team.actionitem;

import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ActionItemAuthorizationService {
    private final ActionItemService actionItemService;
    private final UserMappingAuthorizationService userMappingAuthorizationService;

    public ActionItemAuthorizationService(ActionItemService actionItemService, UserMappingAuthorizationService userMappingAuthorizationService) {
        this.actionItemService = actionItemService;
        this.userMappingAuthorizationService = userMappingAuthorizationService;
    }

    public boolean canUserModifyActionItem(Authentication authentication, UUID actionItemId) {
        var actionItem = actionItemService.getActionItem(actionItemId);
        return actionItem.isPresent() && userMappingAuthorizationService.isUserMemberOfTeam(authentication, actionItem.get().getTeamId());
    }
}
