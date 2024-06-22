package io.nickreuter.retroapi.retro.thought;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/retros/{retroId}/thoughts")
public class ThoughtController {
    private final ThoughtService thoughtService;

    public ThoughtController(ThoughtService thoughtService) {
        this.thoughtService = thoughtService;
    }

    @PostMapping
    @PreAuthorize("@retroAuthorizationService.isUserAllowedInRetro(authentication, #teamId, #retroId)")
    public ResponseEntity<Void> createThought(@PathVariable UUID teamId, @PathVariable UUID retroId, @RequestBody CreateThoughtRequest request) {
        var savedThought = thoughtService.createThought(retroId, request.message(), request.category());
        return ResponseEntity.created(URI.create("/api/teams/%s/retros/%s/thoughts/%s".formatted(teamId, retroId, savedThought.getId()))).build();
    }
}
