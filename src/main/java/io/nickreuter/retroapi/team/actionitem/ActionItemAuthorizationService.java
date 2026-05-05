package io.nickreuter.retroapi.team.actionitem;

import io.nickreuter.retroapi.team.apitoken.authentication.ApiTokenAuthentication;
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
        var maybe = actionItemService.getActionItem(actionItemId);
        if (maybe.isEmpty()) return false;
        var teamId = maybe.get().getTeamId();
        if (authentication instanceof ApiTokenAuthentication tokenAuth) {
            return tokenAuth.getTeamId().equals(teamId) && tokenAuth.getScopes().contains("write");
        }
        return userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId);
    }
}
