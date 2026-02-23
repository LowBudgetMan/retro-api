package io.nickreuter.retroapi.share;

import io.nickreuter.retroapi.retro.RetroService;
import io.nickreuter.retroapi.retro.anonymousparticipant.ShareTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/share")
public class ShareLinkController {
    private final ShareTokenService shareTokenService;
    private final RetroService retroService;

    public ShareLinkController(ShareTokenService shareTokenService, RetroService retroService) {
        this.shareTokenService = shareTokenService;
        this.retroService = retroService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<ShareLinkResponse> validateShareLink(@PathVariable String token) {
        var maybeShareToken = shareTokenService.getShareToken(token);
        if (maybeShareToken.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var shareToken = maybeShareToken.get();
        var maybeRetro = retroService.getRetro(shareToken.retroId());
        if (maybeRetro.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var retro = maybeRetro.get();
        return ResponseEntity.ok(new ShareLinkResponse(retro.teamId(), retro.id()));
    }

    public record ShareLinkResponse(UUID teamId, UUID retroId) {}
}
