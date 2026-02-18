package io.nickreuter.retroapi.retro.anonymousparticipant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/teams/{teamId}/retros/{retroId}/share-tokens")
public class ShareTokenController {
    private ShareTokenService shareTokenService;

    @Autowired
    public ShareTokenController(ShareTokenService shareTokenService) {
        this.shareTokenService = shareTokenService;
    }

    @PostMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
//    @PreAuthorize("@retroAuthorizationService.isUserAllowedInRetro(authentication, #retroId)")
    public ResponseEntity<Void> createShareToken() throws Exception {
        return ResponseEntity.created(new URI("/THing")).build();
    }
}
