package io.nickreuter.retroapi.share;

import io.nickreuter.retroapi.retro.RetroAuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/retros/{retroId}/share-links")
public class ShareController {
    
    private final ShareTokenService shareTokenService;
    private final RetroAuthorizationService retroAuthorizationService;
    
    public ShareController(ShareTokenService shareTokenService, RetroAuthorizationService retroAuthorizationService) {
        this.shareTokenService = shareTokenService;
        this.retroAuthorizationService = retroAuthorizationService;
    }
    
    @PostMapping
    @PreAuthorize("@retroAuthorizationService.isUserAllowedInRetro(authentication, #retroId)")
    public ResponseEntity<ShareTokenResponse> createShareLink(
            @PathVariable UUID retroId,
            @RequestBody(required = false) CreateShareLinkRequest request,
            Authentication authentication) {
        
        // Extract user ID from authentication
        UUID createdBy = UUID.fromString(authentication.getName());
        
        Duration expiration = request != null ? request.expiration() : null;
        Integer maxUses = request != null ? request.maxUses() : null;
        
        ShareToken shareToken = shareTokenService.createShareToken(retroId, createdBy, expiration, maxUses);
        
        // Generate the full share link URL
        String baseUrl = "/share/retro/" + shareToken.token();
        ShareTokenResponse response = new ShareTokenResponse(
            shareToken.id(),
            baseUrl,
            shareToken.expiresAt(),
            shareToken.maxUses(),
            shareToken.uses(),
            shareToken.active()
        );
        
        return ResponseEntity.created(URI.create("/api/retros/%s/share-links/%s".formatted(retroId, shareToken.id())))
                .body(response);
    }
    
    @GetMapping
    @PreAuthorize("@retroAuthorizationService.isUserAllowedInRetro(authentication, #retroId)")
    public ResponseEntity<List<ShareTokenResponse>> getShareLinks(@PathVariable UUID retroId) {
        List<ShareToken> shareTokens = shareTokenService.getActiveShareTokensForRetro(retroId);
        
        List<ShareTokenResponse> responses = shareTokens.stream()
                .map(token -> new ShareTokenResponse(
                    token.id(),
                    "/share/retro/" + token.token(),
                    token.expiresAt(),
                    token.maxUses(),
                    token.uses(),
                    token.active()
                ))
                .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    @DeleteMapping("/{tokenId}")
    @PreAuthorize("@retroAuthorizationService.isUserAllowedInRetro(authentication, #retroId)")
    public ResponseEntity<Void> deactivateShareLink(@PathVariable UUID retroId, @PathVariable UUID tokenId) {
        shareTokenService.deactivateToken(tokenId);
        return ResponseEntity.noContent().build();
    }
    
    public record CreateShareLinkRequest(
        Duration expiration,
        Integer maxUses
    ) {}
    
    public record ShareTokenResponse(
        UUID id,
        String shareUrl,
        java.time.Instant expiresAt,
        Integer maxUses,
        Integer uses,
        boolean active
    ) {}
}
