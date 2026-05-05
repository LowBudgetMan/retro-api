package io.nickreuter.retroapi.team.apitoken;

import io.nickreuter.retroapi.team.apitoken.authentication.ApiTokenAuthentication;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TeamApiAuthorizationService {
    private final UserMappingAuthorizationService userMappingAuthorizationService;

    public TeamApiAuthorizationService(UserMappingAuthorizationService userMappingAuthorizationService) {
        this.userMappingAuthorizationService = userMappingAuthorizationService;
    }

    public boolean canRead(Authentication authentication, UUID teamId) {
        return canPerform(authentication, teamId, "read");
    }

    public boolean canWrite(Authentication authentication, UUID teamId) {
        return canPerform(authentication, teamId, "write");
    }

    private boolean canPerform(Authentication authentication, UUID teamId, String scope) {
        if (authentication instanceof ApiTokenAuthentication tokenAuth) {
            return tokenAuth.getTeamId().equals(teamId) && tokenAuth.getScopes().contains(scope);
        }
        return userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId);
    }
}
