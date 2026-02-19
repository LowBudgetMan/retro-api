package io.nickreuter.retroapi.retro.anonymousparticipant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/retros/{retroId}/share-tokens")
public class ShareTokenController {
    private final ShareTokenService shareTokenService;

    @Autowired
    public ShareTokenController(ShareTokenService shareTokenService) {
        this.shareTokenService = shareTokenService;
    }

    @PostMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> createShareToken(@PathVariable("teamId") UUID teamId, @PathVariable("retroId") UUID retroId) throws Exception {
        return ResponseEntity.created(new URI(shareTokenService.createShareToken(retroId).token())).build();
    }
}
