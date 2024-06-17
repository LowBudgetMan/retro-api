package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RetroAuthorizationService {

    private final UserMappingAuthorizationService userMappingAuthorizationService;
    private final RetroService retroService;

    public RetroAuthorizationService(UserMappingAuthorizationService userMappingAuthorizationService, RetroService retroService) {
        this.userMappingAuthorizationService = userMappingAuthorizationService;
        this.retroService = retroService;
    }

    public boolean isUserAllowedInRetro(Authentication authentication, UUID teamId, UUID retroId) {
        if (userMappingAuthorizationService.isUserMemberOfTeam(authentication, teamId)) {
            var retro = retroService.getRetro(retroId);
            return retro.isPresent() && retro.get().teamId().equals(teamId);
        }
        return false;
    }
}
