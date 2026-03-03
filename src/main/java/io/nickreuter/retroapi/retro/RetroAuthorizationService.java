package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.anonymousparticipant.ShareTokenService;
import io.nickreuter.retroapi.share.authentication.ShareTokenAuthentication;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RetroAuthorizationService {

    private final UserMappingAuthorizationService userMappingAuthorizationService;
    private final RetroService retroService;
    private final ShareTokenService shareTokenService;
    private final HttpServletRequest httpServletRequest;

    public RetroAuthorizationService(UserMappingAuthorizationService userMappingAuthorizationService, RetroService retroService, ShareTokenService shareTokenService, HttpServletRequest httpServletRequest) {
        this.userMappingAuthorizationService = userMappingAuthorizationService;
        this.retroService = retroService;
        this.shareTokenService = shareTokenService;
        this.httpServletRequest = httpServletRequest;
    }

    public boolean isUserAllowedInRetro(Authentication authentication, UUID retroId) {
        var retro = retroService.getRetro(retroId);
        if (retro.isEmpty()) {
            return false;
        }

        // Handle share token authentication (anonymous users)
        if (authentication instanceof ShareTokenAuthentication) {
            ShareTokenAuthentication shareAuth = (ShareTokenAuthentication) authentication;
            return shareAuth.getRetroId().equals(retroId);
        }

        // Handle regular JWT authentication
        if (userMappingAuthorizationService.isUserMemberOfTeam(authentication, retro.get().teamId())) {
            return true;
        }

        // Fallback: JWT user not on team, check for X-Share-Token header
        var shareTokenHeader = httpServletRequest.getHeader("X-Share-Token");
        if (shareTokenHeader == null) {
            return false;
        }
        return shareTokenService.getShareToken(shareTokenHeader)
                .map(shareToken -> shareToken.retroId().equals(retroId))
                .orElse(false);
    }
}
