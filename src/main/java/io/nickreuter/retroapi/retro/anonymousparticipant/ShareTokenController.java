package io.nickreuter.retroapi.retro.anonymousparticipant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
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
    public ResponseEntity<ShareToken> createShareToken(@PathVariable UUID teamId, @PathVariable UUID retroId) throws Exception {
        var shareToken = shareTokenService.createShareToken(retroId);
        return ResponseEntity.created(new URI(shareToken.token())).body(shareToken);
    }

    @GetMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<List<ShareToken>> getShareTokens(@PathVariable UUID teamId, @PathVariable UUID retroId) {
        return ResponseEntity.ok(shareTokenService.getShareTokensForRetro(retroId));
    }
}
